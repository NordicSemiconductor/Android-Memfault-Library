package no.nordicsemi.memfault.observability.bluetooth

import no.nordicsemi.android.ble.ktx.state.ConnectionState

/**
 * Represents the state of the device Bluetooth LE connection.
 */
sealed class DeviceState {
	/** The device is not connected. */
	data object Idle : DeviceState()
	/** The device is currently connecting. */
	data object Connecting : DeviceState()
	/** The device is connected and ready */
	data object Connected : DeviceState()
	/** The device is currently disconnecting. */
	data object Disconnecting : DeviceState()
	/** The device has disconnected unexpectedly or has failed to connect. */
	data class Disconnected(val reason: Reason) : DeviceState() {

		enum class Reason {
			TIMEOUT,
			FAILED_TO_CONNECT,
			NOT_SUPPORTED,
			CONNECTION_LOST;

			override fun toString() = when (this) {
				TIMEOUT           -> "Connection timeout"
				FAILED_TO_CONNECT -> "Failed to connect"
				NOT_SUPPORTED     -> "Not supported"
				CONNECTION_LOST   -> "Connection lost"
			}
		}

		override fun toString(): String {
			return reason.toString()
		}
	}

	/**
	 * Returns whether the device is currently in a state that does allow for connection.
	 */
    fun canConnect(): Boolean = this is Idle || this is Disconnected
}

internal fun ConnectionState.toDeviceState(): DeviceState = when (this) {
	is ConnectionState.Disconnected ->
		when (reason) {
			ConnectionState.Disconnected.Reason.SUCCESS,
			ConnectionState.Disconnected.Reason.CANCELLED ->
				DeviceState.Idle
			ConnectionState.Disconnected.Reason.TIMEOUT ->
				DeviceState.Disconnected(DeviceState.Disconnected.Reason.TIMEOUT)
			ConnectionState.Disconnected.Reason.NOT_SUPPORTED ->
				DeviceState.Disconnected(DeviceState.Disconnected.Reason.NOT_SUPPORTED)
			ConnectionState.Disconnected.Reason.TERMINATE_PEER_USER,
			ConnectionState.Disconnected.Reason.TERMINATE_LOCAL_HOST,
			ConnectionState.Disconnected.Reason.LINK_LOSS ->
				DeviceState.Disconnected(DeviceState.Disconnected.Reason.CONNECTION_LOST)
			ConnectionState.Disconnected.Reason.UNKNOWN ->
				DeviceState.Disconnected(DeviceState.Disconnected.Reason.FAILED_TO_CONNECT)
		}
	ConnectionState.Disconnecting -> DeviceState.Disconnecting
	ConnectionState.Connecting    -> DeviceState.Connecting
	ConnectionState.Initializing  -> DeviceState.Connecting
	ConnectionState.Ready         -> DeviceState.Connected
}

//internal fun ConnectionState.toDeviceState(): DeviceState = when (this) {
//	ConnectionState.Closed  -> DeviceState.Idle
//	is ConnectionState.Disconnected ->
//		when (reason) {
//			ConnectionState.Disconnected.Reason.Success,
//            ConnectionState.Disconnected.Reason.Cancelled ->
//				DeviceState.Idle
//			is ConnectionState.Disconnected.Reason.Timeout ->
//				DeviceState.Disconnected(DeviceState.Disconnected.Reason.TIMEOUT)
//			ConnectionState.Disconnected.Reason.NOT_SUPPORTED ->
//				DeviceState.Disconnected(DeviceState.Disconnected.Reason.NOT_SUPPORTED)
//			ConnectionState.Disconnected.Reason.TerminatePeerUser,
//            ConnectionState.Disconnected.Reason.TerminateLocalHost,
//			ConnectionState.Disconnected.Reason.LinkLoss ->
//				DeviceState.Disconnected(DeviceState.Disconnected.Reason.CONNECTION_LOST)
//			ConnectionState.Disconnected.Reason.UnsupportedAddress,
//			is ConnectionState.Disconnected.Reason.Unknown ->
//				DeviceState.Disconnected(DeviceState.Disconnected.Reason.FAILED_TO_CONNECT)
//		}
//	ConnectionState.Disconnecting -> DeviceState.Disconnecting
//	ConnectionState.Connecting    -> DeviceState.Connecting
//	ConnectionState.Connected     -> DeviceState.Connected
//}