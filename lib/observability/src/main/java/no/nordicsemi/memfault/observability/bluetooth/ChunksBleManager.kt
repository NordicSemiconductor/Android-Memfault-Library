/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

@file:OptIn(ExperimentalUuidApi::class)
@file:Suppress("unused")

package no.nordicsemi.memfault.observability.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.native
import no.nordicsemi.kotlin.ble.client.exception.ConnectionFailedException
import no.nordicsemi.kotlin.ble.core.BondState
import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Manager
import no.nordicsemi.kotlin.ble.core.WriteType
import no.nordicsemi.memfault.observability.data.MemfaultConfig
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Memfault Diagnostics Service UUID.
 *
 * Find specification ih the [Documentation](https://docs.memfault.com/docs/mcu/mds).
 */
val MDS_SERVICE_UUID = Uuid.parse("54220000-f6a5-4007-a371-722f4ebd8436")

private val MDS_SUPPORTED_FEATURES_CHARACTERISTIC_UUID = Uuid.parse("54220001-f6a5-4007-a371-722f4ebd8436")
private val MDS_DEVICE_ID_CHARACTERISTIC_UUID          = Uuid.parse("54220002-f6a5-4007-a371-722f4ebd8436")
private val MDS_DATA_URI_CHARACTERISTIC_UUID           = Uuid.parse("54220003-f6a5-4007-a371-722f4ebd8436")
private val MDS_AUTHORISATION_CHARACTERISTIC_UUID      = Uuid.parse("54220004-f6a5-4007-a371-722f4ebd8436")
private val MDS_DATA_EXPORT_CHARACTERISTIC_UUID        = Uuid.parse("54220005-f6a5-4007-a371-722f4ebd8436")

/**
 * A manager for the Memfault Diagnostics Service (MDS) that streams data from the device.
 *
 * This class connects to the device, discovers the MDS service, reads the configuration,
 * and streams data chunks from the device.
 */
class ChunksBleManager {
	/**
	 * Creates a new instance of [ChunksBleManager] with the given [CentralManager] and [Peripheral].
	 *
	 * This constructor can be user with a 'native' or 'mock' [CentralManager].
	 *
	 * @param centralManager The central manager to use for connection.
	 * @param peripheral The peripheral to connect to.
	 * @param scope The coroutine scope.
	 */
	constructor(
		centralManager: CentralManager,
		peripheral: Peripheral,
		scope: CoroutineScope,
	) {
		this.scope = scope
		this.centralManager = centralManager
		this.peripheral = peripheral
	}

	/**
	 * Creates a new instance of [ChunksBleManager] with the given [Context] and [BluetoothDevice].
	 *
	 * This constructor is for legacy applications that use the Android Bluetooth API.
	 *
	 * @param context The application context.
	 * @param bluetoothDevice The Bluetooth device to connect to.
	 * @param scope The coroutine scope.
	 */
	constructor(
		context: Context,
		bluetoothDevice: BluetoothDevice,
		scope: CoroutineScope
	) {
		this.scope = scope
		this.centralManager = CentralManager.Factory.native(context, scope)
		this.peripheral = centralManager.getPeripheralById(bluetoothDevice.address)!!
	}

	/** The coroutine scope used for launching flows and coroutines. */
	private val scope: CoroutineScope
	private var job: Job? = null
	/** The central manager used to connect to the device. */
	private val centralManager: CentralManager
	/** The peripheral representing the device to connect to. */
	private val peripheral: Peripheral
	/** A flag set when no MDS service was found. */
	private var notSupported = false
	/** A flag set when the bonding process failed. */
	private var bondingFailed = false

	private val _state = MutableStateFlow<DeviceState>(DeviceState.Disconnected())
	private val _config = MutableStateFlow<MemfaultConfig?>(null)
	private val _chunks = MutableSharedFlow<ByteArray>(
		extraBufferCapacity = 25,
		onBufferOverflow = BufferOverflow.SUSPEND
	)

	/** The current state of the device. */
	val state = _state.asStateFlow()
	/** Memfault configuration read from the Memfault Diagnostics Service. */
	val config = _config.asStateFlow()
	/** A flow of streamed data received from the device. */
	val chunks = _chunks.asSharedFlow()

	/**
	 * Starts the connection to the device and begins observing the Memfault Diagnostics Service.
	 *
	 * Use [close] to stop the connection and cancel all observers.
	 */
	fun start() {
		if (job != null) { return }
		job = scope.launch {
			var connection: Job? = null

			// Start observing the central manager state.
			// If Bluetooth gets disabled, or the manager is closed, we cancel the inner scope
			// to cancel all flow observers.
			centralManager.state
				.onEach {
					when (it) {
						Manager.State.POWERED_ON -> {
							// Central Manager is ready, connect or reconnect to the peripheral.
							assert(connection == null) { "Connection already started" }
							connection = connect(centralManager, peripheral)
						}
						Manager.State.UNKNOWN -> {
							// Central Manager was closed, cancel the scope.
							// This will also cancel the connection if it was started.
							cancel()
						}
						else -> {
							// Cancel the connection.
							// It will be restarted when the Central Manager state changes to POWERED_ON.
							connection?.cancel()
							connection = null
						}
					}
				}
				.launchIn(this)

			try { awaitCancellation() }
			finally {
				job = null
			}
		}
	}

	/**
	 * Closes the open connection.
	 *
	 * If the connection is not started, this method does nothing.
	 */
	fun close() {
		job?.cancel()
		job = null
	}

	private fun CoroutineScope.connect(
		centralManager: CentralManager,
		peripheral: Peripheral,
	) = launch {
		// Observe the peripheral bond state to catch bonding failures.
		var wasBonding = false
		peripheral.bondState
			.onEach { bondState ->
				when (bondState) {
					BondState.BONDING -> wasBonding = true
					BondState.NONE -> {
						if (wasBonding) {
							// This will be reported as bond failure on disconnection.
							bondingFailed = true
							wasBonding = false
						}
					}
					else -> {}
				}

			}
			.launchIn(this)

		// Start observing the peripheral state.
		peripheral.state
			// Buffering, as the state Disconnected -> Closed is emitted immediately.
			.buffer()
			.onEach { state ->
				when (state) {
					// State Closes is emitted immediately after Disconnected.
					// We may ignore it, as the observer already notified about the disconnection.
					is ConnectionState.Closed -> return@onEach

					// Disconnected state is emitted when the connection is lost when the device
					// is not supported (disconnect() method called), or the connection was cancelled
					// by the user.
					is ConnectionState.Disconnected -> {
						_state.emit(state.toDeviceState(notSupported, bondingFailed))
						_config.update { null }
						if (state.reason.isUserInitiated /* or not supported */) {
							// If the disconnection was initiated using disconnect() method,
							// it might have been cancelled, or the device is not supported.
							// Either way, cancel auto-reconnection by cancelling the scope.
							cancel()
						}
					}

					// For all other states, we just emit the state.
					// Note, that ConnectionState.Connecting and ConnectionState.Connected
					// emit DeviceState.Connecting state.
					// States DeviceState.Initializing and DeviceState.Connected are emitted later,
					// when the service is initialized. This is to make sure that not supported
					// devices are not reported as connected.
					else -> {
						_state.emit(state.toDeviceState())
					}
				}
			}
			.launchIn(this)

		// Observe the MDS service.
		// This method can be called before the connection is established. It returns a StateFlow
		// that will emit the discovered services once the connection is established.
		// The initial value is null, indicating that the services are not yet discovered.
		peripheral.services(listOf(MDS_SERVICE_UUID))
			// services() returns a StateFlow which is initialized with null,
			// indicating that the services are not yet discovered. Filter that out.
			.filterNotNull()
			// Initialize the MDS service on each service changed.
			.onEach { services ->
				// When the MDS service is discovered, it will be the only one in the list.
				val mds = services.firstOrNull()

				// Check if the MDS service is supported.
				// The exception will be caught in the catch block below.
				checkNotNull(mds) { "Memfault Diagnostics Service not supported" }

				_state.emit(value = DeviceState.Initializing)

				// This method will throw if any of required characteristic is not supported.
				initialize(mds)

				_state.emit(value = DeviceState.Connected)
			}
			.catch { throwable ->
				notSupported = throwable is IllegalStateException
				peripheral.disconnect()
			}
			.launchIn(this)

		// Connect to the peripheral automatically when the manager is created.
		try {
			// If a device is not bonded, but is advertising with private resolvable address
			// the AutoConnect option will fail throwing ConnectionFailedException.
			centralManager.connect(
				peripheral,
				options = CentralManager.ConnectionOptions.AutoConnect(
					automaticallyRequestHighestValueLength = true
				)
			)
		} catch (e: ConnectionFailedException) {
			// In that case, we try to connect directly. This should initiate boding.
			centralManager.connect(
				peripheral,
				options = CentralManager.ConnectionOptions.Direct(
					automaticallyRequestHighestValueLength = true
				)
			)
		}

		try { awaitCancellation() }
		finally {
			// Make sure the device is disconnected when the scope is cancelled.
			// When it was already disconnected, this is a no-op.
			peripheral.disconnect()
			// The state collection was cancelled together with the scope. Emit the state manually.
			_state.emit(DeviceState.Disconnected())
		}
	}

	@OptIn(ExperimentalStdlibApi::class)
	private suspend fun CoroutineScope.initialize(mds: RemoteService) {
		// Read and emit device configuration.
		val deviceId = mds.deviceIdCharacteristic.read()
			.let { String(it) }
		val url = mds.dataUriCharacteristic.read()
			.let { String(it) }
		val authorisationToken = mds.authorisationCharacteristic.read()
			.let { AuthorisationHeader.parse(it) }
		val config = MemfaultConfig(authorisationToken, url, deviceId)
		_config.update { config }

		// Start listening to data collected by the device.
		mds.dataExportCharacteristic.subscribe()
			.buffer()
			.onEach {  _chunks.emit(it) }
			.launchIn(this)

		// Enable notifications for data export characteristic.
		val enableStreamingCommand = byteArrayOf(0x01)
		mds.dataExportCharacteristic.write(enableStreamingCommand, WriteType.WITH_RESPONSE)
	}

	@Suppress("unused")
	private val RemoteService.supportedFeaturesCharacteristic
		get() = characteristics
			.find { it.uuid == MDS_SUPPORTED_FEATURES_CHARACTERISTIC_UUID }
			.let { checkNotNull(it) { "Supported Features characteristic not found" } }
			.also { check(it.properties.contains(CharacteristicProperty.READ)) { "Supported Features characteristic does not have READ property" } }

	private val RemoteService.deviceIdCharacteristic
		get() = characteristics
			.find { it.uuid == MDS_DEVICE_ID_CHARACTERISTIC_UUID }
			.let { checkNotNull(it) { "Device ID characteristic not found" } }
			.also { check(it.properties.contains(CharacteristicProperty.READ)) { "Device ID characteristic does not have READ property" } }

	private val RemoteService.dataUriCharacteristic
		get() = characteristics
			.find { it.uuid == MDS_DATA_URI_CHARACTERISTIC_UUID }
			.let { checkNotNull(it) { "Data URI characteristic not found" } }
			.also { check(it.properties.contains(CharacteristicProperty.READ)) { "Data URI characteristic does not have READ property" } }

	private val RemoteService.authorisationCharacteristic
		get() = characteristics
			.find { it.uuid == MDS_AUTHORISATION_CHARACTERISTIC_UUID }
			.let { checkNotNull(it) { "Authorisation characteristic not found" } }
			.also { check(it.properties.contains(CharacteristicProperty.READ)) { "Authorisation characteristic does not have READ property" } }

	private val RemoteService.dataExportCharacteristic
		get() = characteristics
			.find { it.uuid == MDS_DATA_EXPORT_CHARACTERISTIC_UUID }
			.let { checkNotNull(it) { "Data Export characteristic not found" } }
			.also { check(it.properties.contains(CharacteristicProperty.WRITE)) { "Data Export characteristic does not have WRITE property" } }
			.also { check(it.properties.contains(CharacteristicProperty.NOTIFY)) { "Data Export characteristic does not have NOTIFY property" } }
}