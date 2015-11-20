package SpRT.app;

import java.nio.channels.SelectionKey;


public interface TCPProtocol {
	void handleAccept(SelectionKey key);
	void handleRead(SelectionKey key);
	void handleWrite(SelectionKey key);
}
