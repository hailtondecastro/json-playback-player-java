package org.jsonplayback.player.util;

import static org.hamcrest.CoreMatchers.notNullValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.Assert;

/**
 * Classe auxiliar usada para inspecionar o DML que o NHibernate esta
 * executando.
 * 
 * @author Hailton de Castro
 *
 */
public class SqlLogInspetor implements Appender {
	void aaa() {
		// LoggerFactory.getILoggerFactory().
	}

	private List<String> sqlStatments = new ArrayList<String>();

	/**
	 * Statments executados e logado pelo NHibernate
	 */
	public List<String> getSqlStatments() {
		return sqlStatments;
	}

//	@Override
//	public void doAppend(LoggingEvent event) {
//		boolean doAppendOnOriginal = true;
//		if ("org.hibernate.SQL".equals(event.getLoggerName())) {
//			this.sqlStatments.add(event.getMessage().toString());
//			if (this.levelOriginal != null && event.getLevel().toInt() < this.levelOriginal.toInt()) {
//				doAppendOnOriginal = false;
//			}
//		}
//		if (doAppendOnOriginal && this.appendersRootOriginalList != null) {
//			for (Appender appenderItem : this.appendersRootOriginalList) {
//				appenderItem.doAppend(event);
//			}
//		}
//	}

	private org.apache.logging.log4j.Level levelOriginal = null;

	private List<Appender> appendersRootOriginalList = null;

	/// <summary>
	///
	///
	/// </summary>
	/**
	 * HAbilita a captura dos comandos SQL do NHibernate. Chama
	 * {@code this.SqlStatments.Clear()}
	 */
	public void enable() {
		this.validateEnable();
		this.getSqlStatments().clear();
		org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger("org.hibernate.SQL");

		// 10/10/2016: era usado o logger para adicionar o appender. Mas para
		// evitar mensagens de debug indesejadas usaremos
		//((AppenderAttachable) logger).addAppender(this);

		// INI: 10/10/2016: Mantendo o nivel de log original para evitar
		// mensagens no log que nao deveriam sair no caso de org.hibernate.SQL
		// estar maior que DEBUG.
		// nao funcionou, nao funcionar readicionar os loggers de volta ao root
		// this.appendersRootOriginalList =
		// this.appenderCollectionAsList(((log4net.Repository.Hierarchy.Hierarchy)(logger.Repository)).Root.Appenders);
		// ((log4net.Repository.Hierarchy.Hierarchy)(logger.Repository)).Root.RemoveAllAppenders();
		// ((log4net.Repository.Hierarchy.Hierarchy)(logger.Repository)).Root.AddAppender(this);
		// FIM: 10/10/2016: Mantendo o nivel de log original para evitar
		// mensagens no log que nao deveriam sair no caso de org.hibernate.SQL
		// estar maior que DEBUG.

		this.levelOriginal = logger.getLevel();
		org.apache.logging.log4j.core.config.Configurator.setLevel("com.example.Foo", org.apache.logging.log4j.Level.DEBUG);
		
		LoggerContext lc = (LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
		lc.getConfiguration().addLoggerAppender((org.apache.logging.log4j.core.Logger)logger, this);
		lc.getConfiguration().getLoggerConfig("org.hibernate.SQL").removeAppender(this.getClass().getName());
		
		org.apache.logging.log4j.core.config.Configurator.setLevel("org.hibernate.SQL", org.apache.logging.log4j.Level.DEBUG);
	}

	/**
	 * acho que em java isso nao faz sentido.
	 * 
	 * @param appenderCollection
	 * @return
	 */
	private List<Appender> appenderCollectionAsList(Collection<Appender> appenderCollection) {
		List<Appender> appenderList = new ArrayList<Appender>();
		for (Appender appenderItem : appenderCollection) {
			appenderList.add(appenderItem);
		}
		return appenderList;
	}

	private void validateEnable() {
		// log4net.Core.ILogger logger =
		// (log4net.Core.ILogger)log4net.Core.LoggerManager.GetLogger(this.GetType().Assembly,
		// "org.hibernate.SQL");
		// foreach (IAppender appenderItem in
		// ((log4net.Repository.Hierarchy.Hierarchy)(logger.Repository)).Root.Appenders)
		// {
		// if (appenderItem is SqlLogInspetor)
		// {
		// throw new InvalidOperationException("SqlLogInspetor.Enable() foi
		// chamado mas em uma chamada a SqlLogInspetor.Disable() não foi chamado
		// no ultimo uso!");
		// }
		// }
	}

	/**
	 * Desabilita a captura dos comandos SQL do NHibernate.
	 */
	public void disable() {
		org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger("org.hibernate.SQL");		

		// 10/10/2016: Mantendo o nivel de log original para evitar mensagens no
		// log que nao deveriam sair no caso de org.hibernate.SQL estar maior
		// que DEBUG.
		// ((log4net.Core.IAppenderAttachable)logger).RemoveAppender(this);

		// INI: 10/10/2016: Mantendo o nivel de log original para evitar
		// mensagens no log que nao deveriam sair no caso de org.hibernate.SQL
		// estar maior que DEBUG.
		// nao funcionou, nao funcionar readicionar os loggers de volta ao root
		// ((log4net.Repository.Hierarchy.Hierarchy)(logger.Repository)).Root.RemoveAppender(this);
		// foreach (IAppender appenderItem in this.appendersRootOriginalList)
		// {
		// ((log4net.Repository.Hierarchy.Hierarchy)(logger.Repository)).Root.AddAppender(appenderItem);
		// }
		// this.appendersRootOriginalList = null;
		// FIM: 10/10/2016: Mantendo o nivel de log original para evitar
		// mensagens no log que nao deveriam sair no caso de org.hibernate.SQL
		// estar maior que DEBUG.

		LoggerContext lc = (LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
		org.apache.logging.log4j.core.config.Configurator.setLevel("com.example.Foo", this.levelOriginal);
		org.apache.logging.log4j.core.config.Configurator.setLevel("org.hibernate.SQL", this.levelOriginal);
		lc.getConfiguration().getLoggerConfig("org.hibernate.SQL").removeAppender(this.getClass().getName());		
	}

	/**
	 * Metodo helper para ajudar a verificar se um consunto de intrucoes (em
	 * Expressao Regular) estao dentro da lista de instrucoes executadas
	 * independete da ordem. OBS: Eh montada uma lista temporaria e a cada item
	 * encontrado ele eh removido para que ele nao seja encontrado duas vezes
	 * para expressoes diferentes.
	 * 
	 * @param rxStrArray
	 */
	public void assertContemRegexs(String[] rxStrArray) {
		Pattern rx = null;
		List<String> sqlStatmentsList = new ArrayList<String>(this.getSqlStatments());
		for (int i = 0; i < rxStrArray.length; i++) {
			rx = Pattern.compile(rxStrArray[i]);
			int index = -1;
			for (int j = 0; j < sqlStatmentsList.size(); j++) {
				String sqlSttItem = sqlStatmentsList.get(j);
				if (rx.matcher(sqlSttItem).matches()) {
					index = j;
				}
			}
			String sqlStatment = null;
			if (index > -1)
				sqlStatment = sqlStatmentsList.get(index);

			Assert.assertThat("Padrao de Statment nao localizado na lista. i=" + i + ": " + rxStrArray[i], sqlStatment,
					notNullValue());
			sqlStatmentsList.remove(index);
		}
	}

	@Override
	public State getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		stopped = true;
	}

	@Override
	public void stop() {
		stopped = true;
	}

	@Override
	public boolean isStarted() {
		return !stopped;
	}

	private boolean stopped = false;
	@Override
	public boolean isStopped() {
		return stopped;
	}

	@Override
	public void append(LogEvent event) {
		boolean doAppendOnOriginal = true;
		if ("org.hibernate.SQL".equals(event.getLoggerName())) {
			this.sqlStatments.add(event.getMessage().toString());
			if (this.levelOriginal != null && (event.getLevel().compareTo(this.levelOriginal) < 0)) {
				doAppendOnOriginal = false;
			}
		}
		if (doAppendOnOriginal && this.appendersRootOriginalList != null) {
			for (Appender appenderItem : this.appendersRootOriginalList) {
				appenderItem.append(event);
			}
		}
		
	}

	private org.apache.logging.log4j.core.Layout<Serializable> layout;
	
	@Override
	public org.apache.logging.log4j.core.Layout<? extends Serializable> getLayout() {
		return layout;
	}

	@Override
	public boolean ignoreExceptions() {
		return false;
	}

	private org.apache.logging.log4j.core.ErrorHandler errorHandler;
	
	@Override
	public org.apache.logging.log4j.core.ErrorHandler getHandler() {
		return errorHandler;
	}

	@Override
	public void setHandler(org.apache.logging.log4j.core.ErrorHandler handler) {
		errorHandler = handler;
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
