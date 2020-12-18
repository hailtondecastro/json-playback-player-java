package org.jsonplayback.player.util;

import org.springframework.boot.logging.AbstractLoggingSystem;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LoggingInitializationContext;

public class NoOpLoggingSystem extends AbstractLoggingSystem {

	public NoOpLoggingSystem(ClassLoader classLoader) {
		super(classLoader);
	}

	@Override
	protected String[] getStandardConfigLocations() {
		return new String[] {};
	}

	@Override
	protected void loadDefaults(LoggingInitializationContext initializationContext, LogFile logFile) {
	}

	@Override
	protected void loadConfiguration(LoggingInitializationContext initializationContext, String location,
			LogFile logFile) {		
	}

}
