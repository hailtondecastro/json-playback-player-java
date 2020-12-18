package org.jsonplayback.player;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

public interface IDirectRawWriter {
	void write(OutputStream outputStream) throws IOException, SQLException;
}
