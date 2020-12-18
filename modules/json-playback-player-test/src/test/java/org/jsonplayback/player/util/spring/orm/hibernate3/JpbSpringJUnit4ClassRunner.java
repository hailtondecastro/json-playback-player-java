package org.jsonplayback.player.util.spring.orm.hibernate3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsonplayback.player.util.NoOpLoggingSystem;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import config.org.jsonplayback.player.PlayerManagerTestConfig;

public class JpbSpringJUnit4ClassRunner extends BlockJUnit4ClassRunner {
	
	private SpringJUnit4ClassRunner wrappedRunner;
	
	public JpbSpringJUnit4ClassRunner(Class<?> clazz) throws InitializationError {
		super(clazz);
		wrappedRunner = new SpringJUnit4ClassRunner(clazz);
	}
	
	static {
		System.setProperty("hsqldb.reconfig_logging", "false");
		System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
		System.setProperty(LoggingSystem.class.getName(), NoOpLoggingSystem.class.getName());
		org.apache.logging.log4j.Logger loggerL4j2 = org.apache.logging.log4j.LogManager.getLogger(PlayerManagerTestConfig.class);
		loggerL4j2.error("log4j 2: Logging test.");
		loggerL4j2 = org.apache.logging.log4j.LogManager.getLogger(PlayerManagerTestConfig.class.getName());
		loggerL4j2.error("log4j 2: Logging test. BY NAME");
		org.apache.log4j.Logger loggerL4j1 = org.apache.log4j.LogManager.getLogger(PlayerManagerTestConfig.class);
		loggerL4j1.error("log4j 1: Logging test.");
		loggerL4j1 = org.apache.log4j.LogManager.getLogger(PlayerManagerTestConfig.class.getName());
		loggerL4j1.error("log4j 1: Logging test. BY NAME");
		Logger loggerSlf4j = LoggerFactory.getLogger(PlayerManagerTestConfig.class);
		loggerSlf4j.error("SLF4J: Logging test.");
		Log loggerCommon = LogFactory.getLog(PlayerManagerTestConfig.class);
		loggerCommon.error("Common logging: Logging test.");
		java.util.logging.Logger loggerJul = java.util.logging.Logger.getLogger(PlayerManagerTestConfig.class.getName());
		loggerJul.log(java.util.logging.Level.SEVERE, "java.util.logging: Logging test.");
		org.jboss.logging.Logger loggerJboss = org.jboss.logging.Logger.getLogger(PlayerManagerTestConfig.class);
		loggerJboss.error("Jboss log manager: Logging test.");
	}

	@Override
	public Description getDescription() {
		// TODO Auto-generated method stub
		return wrappedRunner.getDescription();
	}

	@Override
	public void run(RunNotifier notifier) {
		// TODO Auto-generated method stub
		wrappedRunner.run(notifier);
	}

	@Override
	public void filter(Filter filter) throws NoTestsRemainException {
		// TODO Auto-generated method stub
		wrappedRunner.filter(filter);
	}

	@Override
	public void sort(Sorter sorter) {
		// TODO Auto-generated method stub
		wrappedRunner.sort(sorter);
	}

	@Override
	public void setScheduler(RunnerScheduler scheduler) {
		// TODO Auto-generated method stub
		wrappedRunner.setScheduler(scheduler);
	}

	@Override
	public int testCount() {
		// TODO Auto-generated method stub
		return wrappedRunner.testCount();
	}
	
	
	
}
