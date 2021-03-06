package config.org.jsonplayback.player;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import org.hibernate.SessionFactory;
import org.hsqldb.jdbc.JDBCDataSource;
import org.jsonplayback.hibernate.Hb3Support;
import org.jsonplayback.hibernate.Hb4Support;
import org.jsonplayback.hibernate.Hb5Support;
import org.jsonplayback.jpa.JpaSupport;
import org.jsonplayback.player.IPlayerConfig;
import org.jsonplayback.player.IPlayerManager;
import org.jsonplayback.player.IPlayerSnapshot;
import org.jsonplayback.player.ObjPersistenceMode;
import org.jsonplayback.player.PlayerManagerTest;
import org.jsonplayback.player.PlayerSnapshot;
import org.jsonplayback.player.SignatureBean;
import org.jsonplayback.player.hibernate.entities.DetailAEnt;
import org.jsonplayback.player.hibernate.entities.DetailARefererEnt;
import org.jsonplayback.player.hibernate.entities.MasterAEnt;
import org.jsonplayback.player.hibernate.entities.MasterBEnt;
import org.jsonplayback.player.hibernate.nonentities.DetailAWrapper;
import org.jsonplayback.player.hibernate.nonentities.MasterAWrapper;
import org.jsonplayback.player.implementation.IPlayerManagersHolderImplementor;
import org.jsonplayback.player.implementation.PlayerBeanSerializerModifier;
import org.jsonplayback.player.implementation.PlayerConfig;
import org.jsonplayback.player.implementation.PlayerManagerDefault;
import org.jsonplayback.player.implementation.PlayerManagersHolderDefault;
import org.jsonplayback.player.implementation.PlayerSnapshotSerializer;
import org.jsonplayback.player.spring.context.annotation.OnCustomizedPersistence;
import org.jsonplayback.player.spring.context.annotation.OnHibernate3;
import org.jsonplayback.player.spring.context.annotation.OnHibernate4;
import org.jsonplayback.player.spring.context.annotation.OnHibernate5;
import org.jsonplayback.player.spring.context.annotation.OnJpa;
import org.jsonplayback.player.util.NoOpLoggingSystem;
import org.jsonplayback.player.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Classe de configuracao base para todas (por enquanto) outras classes de
 * configurcao de teste.<br>
 * Uma das principais funcoes dessa classe eh definir os metodos
 * {@link #getJndiDatasourceCacheName()}, {@link #getJndiJwtCacheName()} e
 * {@link #getJndiLocalCacheName()} que geram nomes jndi nao diferentes para
 * cada classe de configuracao de teste.<br>
 * nao deve conter anotacoes de configuracao spring, a nao ser que que todas as
 * possiveis classe base usem sempre o mesmo.
 * 
 * @author 63315947368
 *
 */
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class,
		// veja
		// https://stackoverflow.com/questions/38747548/spring-boot-disable-error-mapping
		JpaRepositoriesAutoConfiguration.class,
		DataSourceAutoConfiguration.class, 
		HibernateJpaAutoConfiguration.class, 
		DataSourceTransactionManagerAutoConfiguration.class, 
		JpaRepositoriesAutoConfiguration.class, 
		CassandraAutoConfiguration.class, 
		CassandraDataAutoConfiguration.class,
		XADataSourceAutoConfiguration.class
		})
@EnableTransactionManagement
@ComponentScan(basePackages = { "org.jsonplayback" }, excludeFilters = {
		@ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
@PropertySource("test-environment.properties")
public class PlayerManagerTestConfig {
	
	static {
	}
	
	public static final String OBJ_PERSISTENCE_MODE = "jsonplayback.objPersistenceMode";
    
	//@Value("${"+OBJ_PERSISTENCE_MODE+"}")
    //protected ObjPersistenceMode objPersistenceMode;
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(PlayerManagerTestConfig.class);
	
    @PostConstruct
	public void init() {
    	System.setProperty("hsqldb.reconfig_logging", "false");
		System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
		System.setProperty(LoggingSystem.class.getName(), NoOpLoggingSystem.class.getName());
    }
    
    @Autowired
    ApplicationContext applicationContext;
    
    
    @SuppressWarnings("deprecation")
	@Bean("localSessionFactoryBean3")
	@Conditional(OnHibernate3.class)
    public Object getSessionFactoryBeanHb3() {
    //public LocalSessionFactoryBean getSessionFactoryBean3() {
    	JDBCDataSource dataSource = new JDBCDataSource();
    	dataSource.setURL("jdbc:hsqldb:mem:js-hb-supersync?hsqldb.sqllog=3");
    	Object localSessionFactoryBean = ReflectionUtil.instanciteByReflection("org.springframework.orm.hibernate3.LocalSessionFactoryBean", new String[]{}, new Object[]{});
    	ReflectionUtil.runByReflection(
    		"org.springframework.orm.hibernate3.LocalSessionFactoryBean",
    		"setDataSource",
			new String[]{"javax.sql.DataSource"},
			localSessionFactoryBean,
			new Object[]{dataSource});
    	ReflectionUtil.runByReflection(
			"org.springframework.orm.hibernate3.LocalSessionFactoryBean",
			"setMappingResources",
			new String[]{String[].class.getName()},
			localSessionFactoryBean,
			new Object[]{
				new String[]{
					PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+"MasterAEnt.hbm.xml",
					PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+"MasterBEnt.hbm.xml",
					PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+"/DetailAEnt.hbm.xml",
					PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+"/DetailARefererEnt.hbm.xml"					
				}
			}
		);
    	Properties hbProperties = new Properties();
    	hbProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
    	hbProperties.setProperty("format_sql", "true");
    	ReflectionUtil.runByReflection(
			"org.springframework.orm.hibernate3.LocalSessionFactoryBean",
			"setHibernateProperties",
			new String[]{"java.util.Properties"},
			localSessionFactoryBean,
			new Object[]{hbProperties}
		);
    	
    	
//    	LocalSessionFactoryBean localSessionFactoryBean = new LocalSessionFactoryBean();
//    	localSessionFactoryBean.setDataSource(dataSource);
//    	localSessionFactoryBean.setMappingResources(new String[]{
//    		"jsonplayback/MasterAEnt.hbm.xml",
//    		"jsonplayback/MasterBEnt.hbm.xml",
//    		"jsonplayback/DetailAEnt.hbm.xml"
//    	});
//    	Properties hbProperties = new Properties();
//    	hbProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
//    	hbProperties.setProperty("format_sql", "true");
//    	localSessionFactoryBean.setHibernateProperties(hbProperties);
    	
    	return localSessionFactoryBean;
    }
 
    @Bean("localSessionFactoryBean4")
	@Conditional(OnHibernate4.class)
    public Object getSessionFactoryBeanHb4() {
    //public org.springframework.orm.hibernate4.LocalSessionFactoryBean getSessionFactoryBean4() {
    	JDBCDataSource dataSource = new JDBCDataSource();
    	dataSource.setURL("jdbc:hsqldb:mem:js-hb-supersync?hsqldb.sqllog=3");
    	
    	Object localSessionFactoryBean = ReflectionUtil.instanciteByReflection("org.springframework.orm.hibernate4.LocalSessionFactoryBean", new String[]{}, new Object[]{});
    	ReflectionUtil.runByReflection(
    		"org.springframework.orm.hibernate4.LocalSessionFactoryBean",
    		"setDataSource",
			new String[]{"javax.sql.DataSource"},
			localSessionFactoryBean,
			new Object[]{dataSource});
    	ReflectionUtil.runByReflection(
			"org.springframework.orm.hibernate4.LocalSessionFactoryBean",
			"setAnnotatedClasses",
			new String[]{Class[].class.getName()},
			localSessionFactoryBean,
			new Object[]{
				new Class<?>[]{
		    		MasterAEnt.class,
		    		MasterBEnt.class,
		    		DetailAEnt.class,
		    		DetailARefererEnt.class
		    	}
			}
		);
    	Properties hbProperties = new Properties();
    	hbProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
    	hbProperties.setProperty("format_sql", "true");
    	ReflectionUtil.runByReflection(
			"org.springframework.orm.hibernate4.LocalSessionFactoryBean",
			"setHibernateProperties",
			new String[]{"java.util.Properties"},
			localSessionFactoryBean,
			new Object[]{hbProperties}
		);
    	
//    	org.springframework.orm.hibernate4.LocalSessionFactoryBean localSessionFactoryBean = new org.springframework.orm.hibernate4.LocalSessionFactoryBean();
//    	localSessionFactoryBean.setDataSource(dataSource);
//    	localSessionFactoryBean.setAnnotatedClasses(new Class<?>[]{
//    		MasterAEnt.class,
//    		MasterBEnt.class,
//    		DetailAEnt.class
//    	});
//    	Properties hbProperties = new Properties();
//    	hbProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
//    	hbProperties.setProperty("format_sql", "true");
//    	localSessionFactoryBean.setHibernateProperties(hbProperties);
    	
    	return localSessionFactoryBean;
    }
 
    @Bean("localSessionFactoryBean5")
	@Conditional(OnHibernate5.class)
    public org.springframework.orm.hibernate5.LocalSessionFactoryBean getSessionFactoryBean5() {
    	
    	JDBCDataSource dataSource = new JDBCDataSource();
    	dataSource.setURL("jdbc:hsqldb:mem:js-hb-supersync?hsqldb.sqllog=3");
    	//CustomLocalSessionFactoryBean customLocalSessionFactoryBean = new CustomLocalSessionFactoryBean();
    	org.springframework.orm.hibernate5.LocalSessionFactoryBean localSessionFactoryBean = new org.springframework.orm.hibernate5.LocalSessionFactoryBean();
    	localSessionFactoryBean.setDataSource(dataSource);
    	localSessionFactoryBean.setAnnotatedClasses(new Class<?>[]{
    		MasterAEnt.class,
    		MasterBEnt.class,
    		DetailAEnt.class,
    		DetailARefererEnt.class
    	});
    	Properties hbProperties = new Properties();
    	hbProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
    	//hbProperties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
    	hbProperties.setProperty("format_sql", "true");
    	localSessionFactoryBean.setHibernateProperties(hbProperties);
    	localSessionFactoryBean.setDataSource(dataSource);
    	
    	return localSessionFactoryBean;
    }

    @Bean("localSessionFactoryBeanJpa")
	@Conditional(OnJpa.class)
    public org.springframework.orm.hibernate5.LocalSessionFactoryBean getSessionFactoryBeanJpa() {
    	
    	JDBCDataSource dataSource = new JDBCDataSource();
    	dataSource.setURL("jdbc:hsqldb:mem:js-hb-supersync?hsqldb.sqllog=3");
    	//CustomLocalSessionFactoryBean customLocalSessionFactoryBean = new CustomLocalSessionFactoryBean();
    	org.springframework.orm.hibernate5.LocalSessionFactoryBean localSessionFactoryBean = new org.springframework.orm.hibernate5.LocalSessionFactoryBean();
    	localSessionFactoryBean.setDataSource(dataSource);
    	localSessionFactoryBean.setAnnotatedClasses(new Class<?>[]{
    		MasterAEnt.class,
    		MasterBEnt.class,
    		DetailAEnt.class,
    		DetailARefererEnt.class
    	});
    	Properties hbProperties = new Properties();
    	hbProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
    	//hbProperties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
    	hbProperties.setProperty("format_sql", "true");
    	localSessionFactoryBean.setHibernateProperties(hbProperties);
    	localSessionFactoryBean.setDataSource(dataSource);
    	
    	return localSessionFactoryBean;
    }
    
    @Bean("localSessionFactoryBeanCustomizedPersistence")
	@Conditional(OnCustomizedPersistence.class)
    public org.springframework.orm.hibernate5.LocalSessionFactoryBean getSessionFactoryBeanCustomizedPersistence() {
    	
    	JDBCDataSource dataSource = new JDBCDataSource();
    	dataSource.setURL("jdbc:hsqldb:mem:js-hb-supersync?hsqldb.sqllog=3");
    	//CustomLocalSessionFactoryBean customLocalSessionFactoryBean = new CustomLocalSessionFactoryBean();
    	org.springframework.orm.hibernate5.LocalSessionFactoryBean localSessionFactoryBean = new org.springframework.orm.hibernate5.LocalSessionFactoryBean();
    	localSessionFactoryBean.setDataSource(dataSource);
    	localSessionFactoryBean.setAnnotatedClasses(new Class<?>[]{
    		MasterAEnt.class,
    		MasterBEnt.class,
    		DetailAEnt.class,
    		DetailARefererEnt.class
    	});
    	Properties hbProperties = new Properties();
    	hbProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
    	//hbProperties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
    	hbProperties.setProperty("format_sql", "true");
    	localSessionFactoryBean.setHibernateProperties(hbProperties);
    	localSessionFactoryBean.setDataSource(dataSource);
    	
    	return localSessionFactoryBean;
    }
    
    @Bean
    @Conditional(OnHibernate3.class)
    public PlatformTransactionManager transactionManagerHb3(SessionFactory sessionFactory) {
    	return (PlatformTransactionManager) ReflectionUtil.instanciteByReflection(
    		"org.springframework.orm.hibernate3.HibernateTransactionManager",
    		new String[]{"org.hibernate.SessionFactory"}, 
    		new Object[]{sessionFactory});
    	//return new org.springframework.orm.hibernate3.HibernateTransactionManager(sessionFactory);
    }
    
    @Bean
    @Conditional(OnHibernate4.class)
    public PlatformTransactionManager transactionManagerHb4(SessionFactory sessionFactory) {
    	return (PlatformTransactionManager) ReflectionUtil.instanciteByReflection(
        		"org.springframework.orm.hibernate4.HibernateTransactionManager",
        		new String[]{"org.hibernate.SessionFactory"}, 
        		new Object[]{sessionFactory});
    	//return new org.springframework.orm.hibernate4.HibernateTransactionManager(sessionFactory);
    }
    
    @Bean
    @Conditional(OnHibernate5.class)
    public PlatformTransactionManager transactionManagerHb5(SessionFactory sessionFactory) {
    	return new org.springframework.orm.hibernate5.HibernateTransactionManager(sessionFactory);
    }
    
    @Bean
    @Conditional(OnJpa.class)
    public PlatformTransactionManager transactionManagerJpa(SessionFactory sessionFactory) {
    	return new org.springframework.orm.hibernate5.HibernateTransactionManager(sessionFactory);
    }
    
    @Bean
    @Conditional(OnCustomizedPersistence.class)
    public PlatformTransactionManager transactionManagerCustomizedPersistence(SessionFactory sessionFactory) {
    	return new org.springframework.orm.hibernate5.HibernateTransactionManager(sessionFactory);
    }
    
	@Bean
	public IPlayerConfig getConfig(
			@Autowired SessionFactory sessionFactory, 
			@Autowired ApplicationContext applicationContext,
			@Value("${"+OBJ_PERSISTENCE_MODE+"}") ObjPersistenceMode objPersistenceMode) {
		IPlayerConfig config = 
				new PlayerConfig()
					.configManagerId("manager01")
					.configObjPersistenceMode(objPersistenceMode)
					.registerAddictionalManagedType(MasterAWrapper.class)
					.registerAddictionalManagedType(DetailAWrapper.class);
		if (objPersistenceMode == ObjPersistenceMode.HB3) {
			config = config.configObjPersistenceSupport(
					new Hb3Support() {						
						@Override
						public SessionFactory getSessionFactory() {
							return (SessionFactory) applicationContext.getBean("localSessionFactoryBean3");
						}
					});			
		} else if (objPersistenceMode == ObjPersistenceMode.HB4) {
			config = config.configObjPersistenceSupport(
					new Hb4Support() {						
						@Override
						public SessionFactory getSessionFactory() {
							return (SessionFactory) applicationContext.getBean("localSessionFactoryBean4");
						}
					});					
		} else if (objPersistenceMode == ObjPersistenceMode.HB5) {
			config = config.configObjPersistenceSupport(
					new Hb5Support() {						
						@Override
						public SessionFactory getSessionFactory() {
							return (SessionFactory) applicationContext.getBean("localSessionFactoryBean5");
						}
					});						
		} else if (objPersistenceMode == ObjPersistenceMode.JPA) {
			config = config.configObjPersistenceSupport(
					new JpaSupport() {
						@Override
						public EntityManager getCurrentEntityManager() {
							return (EntityManager) ((SessionFactory) applicationContext.getBean("localSessionFactoryBeanJpa")).getCurrentSession();
						}
					});				
		} else if (objPersistenceMode == ObjPersistenceMode.CUSTOMIZED_PERSISTENCE) {
			config = config.configObjPersistenceSupport(
					new JpaSupport() {
						@Override
						public EntityManager getCurrentEntityManager() {
							return (EntityManager) ((SessionFactory) applicationContext.getBean("localSessionFactoryBeanCustomizedPersistence")).getCurrentSession();
						}
					});					
		} else {
			throw new RuntimeException("This should not happen. config.getObjPersistenceMode(): " + config.getObjPersistenceMode());
		}
		
		return config;
	}
	
	@Bean
	public IPlayerManager getManager(@Autowired IPlayerConfig config, PlatformTransactionManager transactionManager) {
		IPlayerManager manager = null;
		if (config.getObjPersistenceMode() == ObjPersistenceMode.HB3) {
			manager = new PlayerManagerDefault().configure(config);			
		} else if (config.getObjPersistenceMode() == ObjPersistenceMode.HB4) {
			manager = new PlayerManagerDefault().configure(config);						
		} else if (config.getObjPersistenceMode() == ObjPersistenceMode.HB5) {
			manager = new PlayerManagerDefault().configure(config);						
		} else if (config.getObjPersistenceMode() == ObjPersistenceMode.JPA) {
			manager = new PlayerManagerDefault().configure(config);					
		} else if (config.getObjPersistenceMode() == ObjPersistenceMode.CUSTOMIZED_PERSISTENCE) {
			manager = new PlayerManagerDefault(){
				public SignatureBean deserializeSignature(String signatureStr) {
					return super.deserializeSignature(signatureStr.substring(0, signatureStr.length() - ObjPersistenceMode.CUSTOMIZED_PERSISTENCE.toString().length()));
				};
				public String serializeSignature(SignatureBean signatureBean) {
					return super.serializeSignature(signatureBean)  + ObjPersistenceMode.CUSTOMIZED_PERSISTENCE.toString();					
				};
			}.configure(config);
		} else {
			throw new RuntimeException("This should not happen. config.getObjPersistenceMode(): " + config.getObjPersistenceMode());
		}	
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		final IPlayerManager managerFinal = manager;
		manager = (IPlayerManager) transactionTemplate.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				return managerFinal.init();
			}
		});
		return manager;
	}
	
	@Bean
	public IPlayerManagersHolderImplementor getManagersHolder(@Autowired IPlayerManager manager) {
		IPlayerManagersHolderImplementor managersHolderImplementor = new PlayerManagersHolderDefault();
		managersHolderImplementor.addManager(manager);
		return managersHolderImplementor;
	}
	
	@Bean
	public PlayerSnapshotSerializer getPlayerSnapshotSerializer(@Autowired IPlayerManagersHolderImplementor managersHolder) {
		PlayerSnapshotSerializer serializer = new PlayerSnapshotSerializer().configManagerHolder(managersHolder);
		return serializer;
	}
	
	/**
	 * Esse bean nao eh realmente usado, somente injetamos no {@link ObjectMapper}
	 * @param manager
	 * @param mapper
	 * @param jsonComponentModule
	 * @return
	 */
	@Bean
	public PlayerBeanSerializerModifier getPlayerBeanSerializerModifier(
			@Autowired IPlayerManagersHolderImplementor managersHolder, 
			@Autowired PlayerSnapshotSerializer playerSnapshotSerializer, 
			@Autowired Jackson2ObjectMapperBuilder builder,
			@Autowired JsonComponentModule module,
			@Autowired MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter
//			@Autowired ObjectMapper mapperNovo
			) {
		PlayerBeanSerializerModifier modifier = new PlayerBeanSerializerModifier().configManagerHolder(managersHolder);
		//jsonComponentModule.addSerializer(PlayerSnapshot.class, playerSnapshotSerializer);
		//mapper.registerModule(jsonComponentModule);
		module.addSerializer(IPlayerSnapshot.class, playerSnapshotSerializer);
		module.setSerializerModifier(modifier);
		ObjectMapper mapperOriginal = mappingJackson2HttpMessageConverter.getObjectMapper();
		ObjectMapper mapperNovo = builder.build();
		mapperNovo.setConfig(mapperNovo.getSerializationConfig().with(managersHolder.getClassIntrospertor()));
		mapperNovo.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapperNovo.enable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
		if (mapperOriginal != mapperNovo) {
			logger.warn("(mapperOriginal != mapperNovo) apos org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.build()");
		}
		if (mappingJackson2HttpMessageConverter.getObjectMapper() != mapperNovo) {
			logger.warn("(mappingJackson2HttpMessageConverter.getObjectMapper() != mapperNovo) apos org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.build()");
		}
		mappingJackson2HttpMessageConverter.setObjectMapper(mapperNovo);
		for (IPlayerManager manager : managersHolder.managers()) {
			manager.getConfig().configObjectMapper(mapperNovo);			
		}
		
		return modifier;
	}
}
