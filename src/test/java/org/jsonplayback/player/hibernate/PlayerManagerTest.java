package org.jsonplayback.player.hibernate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.config.jsonplayback.TestServiceConfigBase;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.jsonplayback.player.IPlayerManager;
import org.jsonplayback.player.ObjPersistenceSupport;
import org.jsonplayback.player.PlayerSnapshot;
import org.jsonplayback.player.SignatureBean;
import org.jsonplayback.player.hibernate.entities.DetailAComp;
import org.jsonplayback.player.hibernate.entities.DetailACompComp;
import org.jsonplayback.player.hibernate.entities.DetailACompId;
import org.jsonplayback.player.hibernate.entities.DetailAEnt;
import org.jsonplayback.player.hibernate.entities.MasterAEnt;
import org.jsonplayback.player.hibernate.entities.MasterBComp;
import org.jsonplayback.player.hibernate.entities.MasterBCompComp;
import org.jsonplayback.player.hibernate.entities.MasterBCompId;
import org.jsonplayback.player.hibernate.entities.MasterBEnt;
import org.jsonplayback.player.hibernate.nonentities.DetailAWrapper;
import org.jsonplayback.player.hibernate.nonentities.MasterAWrapper;
import org.jsonplayback.player.implementation.IPlayerManagerImplementor;
import org.jsonplayback.player.util.ReflectionUtil;
import org.jsonplayback.player.util.SqlLogInspetor;
import org.jsonplayback.player.util.spring.orm.hibernate3.JpbSpringJUnit4ClassRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

@ContextConfiguration(classes=TestServiceConfigBase.class)
@RunWith(JpbSpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners={DependencyInjectionTestExecutionListener.class})
public class PlayerManagerTest {
	static {
	}
	
	public static final Logger log = LoggerFactory.getLogger(PlayerManagerTest.class);
	
	@Autowired
	private SessionFactory sessionFactory;
	
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    	System.out.println("TEST");
    }
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }
    
    @Autowired
    private HibernateJpaCompat hibernateJpaCompat;
    
    @Autowired
    IPlayerManager manager;
    
    @SuppressWarnings("unchecked")
	private void createDataBaseStructures() {
    	
    	if (this.getLocalSessionFactoryBean3() != null) {
    		ReflectionUtil.runByReflection(
    			"org.springframework.orm.hibernate3.LocalSessionFactoryBean",
    			"dropDatabaseSchema", 
    			new String[]{},
    			this.getLocalSessionFactoryBean3(),
    			new Object[]{}
    		);
    		ReflectionUtil.runByReflection(
    			"org.springframework.orm.hibernate3.LocalSessionFactoryBean",
    			"createDatabaseSchema", 
    			new String[]{},
    			this.getLocalSessionFactoryBean3(),
    			new Object[]{}
    		);
//    		this.localSessionFactoryBean.dropDatabaseSchema();			
//    		this.localSessionFactoryBean.createDatabaseSchema();   	
    	} else if (this.getLocalSessionFactoryBean4() != null) {
    		Object configuration =
    			ReflectionUtil.runByReflection(
        			"org.springframework.orm.hibernate4.LocalSessionFactoryBean",
        			"getConfiguration",
        			new String[]{},
        			this.getLocalSessionFactoryBean4(),
        			new Object[]{});
    		SchemaExport export =
    			(SchemaExport) ReflectionUtil
	    				.instanciteByReflection(
	    					"org.hibernate.tool.hbm2ddl.SchemaExport",
	    					new String[]{"org.hibernate.cfg.Configuration"},
	    					new Object[]{configuration});
    		ReflectionUtil.runByReflection(
    			"org.hibernate.tool.hbm2ddl.SchemaExport",
    			"drop",
    			new String[]{ boolean.class.getName(), boolean.class.getName() },
    			export,
    			new Object[]{ false, true });
    		ReflectionUtil.runByReflection(
        			"org.hibernate.tool.hbm2ddl.SchemaExport",
        			"create",
        			new String[]{ boolean.class.getName(), boolean.class.getName() },
        			export,
        			new Object[]{ false, true });
//    		SchemaExport export = new SchemaExport(this.localSessionFactoryBean4.getConfiguration());
//    	    export.drop(false, true);
//    	    export.create(false, true);
    	} else if (this.getLocalSessionFactoryBean5OrJpa() != null) {
    		Object configuration = ReflectionUtil.runByReflection(
				this.getLocalSessionFactoryBean5OrJpa().getClass().getName(),
    			"getConfiguration",
    			new String[]{},
    			this.getLocalSessionFactoryBean5OrJpa(),
    			new Object[]{}
        	);
    		Object standardServiceRegistryBuilder = ReflectionUtil.runByReflection(
				configuration.getClass().getName(),
    			"getStandardServiceRegistryBuilder",
    			new String[]{},
    			configuration,
    			new Object[]{}
        	);
    		Object standardServiceRegistry = ReflectionUtil.runByReflection(
				"org.hibernate.boot.registry.StandardServiceRegistryBuilder",
    			"build",
    			new String[]{},
    			standardServiceRegistryBuilder,
    			new Object[]{}
        	);
    		Object metadataSources = ReflectionUtil.runByReflection(
				this.getLocalSessionFactoryBean5OrJpa().getClass().getName(),
    			"getMetadataSources",
    			new String[]{},
    			this.getLocalSessionFactoryBean5OrJpa(),
    			new Object[]{}
        	);
    		Object hb5Metadata = ReflectionUtil.runByReflection(
    			"org.hibernate.boot.MetadataSources",
    			"buildMetadata",
    			new String[]{"org.hibernate.boot.registry.StandardServiceRegistry"},
    			metadataSources,
    			new Object[]{standardServiceRegistry}
        	);
    		Object export = ReflectionUtil.instanciteByReflection(
   				"org.hibernate.tool.hbm2ddl.SchemaExport",
    			new String[]{},
    			new Object[]{}
    		);
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Class<Enum> targetTypeClass = (Class<Enum>) ReflectionUtil.correctClass("org.hibernate.tool.schema.TargetType");
    		ReflectionUtil.runByReflection(
				"org.hibernate.tool.hbm2ddl.SchemaExport",
				"drop",
				new String[]{
					java.util.EnumSet.class.getName(),
					"org.hibernate.boot.Metadata"
				},
				export,
				new Object[]{
					EnumSet.of(Enum.valueOf(targetTypeClass, "DATABASE")),
					hb5Metadata
				}
	    	);
			ReflectionUtil.runByReflection(
				"org.hibernate.tool.hbm2ddl.SchemaExport",
				"create",
				new String[]{
					java.util.EnumSet.class.getName(),
					"org.hibernate.boot.Metadata"
				},
				export,
				new Object[]{
					EnumSet.of(Enum.valueOf(targetTypeClass, "DATABASE")),
					hb5Metadata
				}
	    	);
    		
//    		org.hibernate.boot.registry.StandardServiceRegistry standardServiceRegistry = this.localSessionFactoryBean5.getConfiguration().getStandardServiceRegistryBuilder().build();
//    		MetadataSources metadataSources = this.localSessionFactoryBean5.getMetadataSources();
//    		Metadata hb5Metadata = metadataSources.buildMetadata(standardServiceRegistry);
//    		SchemaExport export = new SchemaExport();
//    		export.drop(EnumSet.of(TargetType.DATABASE), hb5Metadata);
//    		export.create(EnumSet.of(TargetType.DATABASE), hb5Metadata);    		
    	}
    }
    
    @Before
    public void setUp() throws Exception {
    	this.createDataBaseStructures();
    	
    	//System.setProperty("user.timezone", "GMT");
    	java.util.TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
		
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus status) {
				Session ss = PlayerManagerTest.this.sessionFactory.getCurrentSession();
				try {
					//SchemaExport
					//tx = ss.beginTransaction();
					
					//SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
					//sqlLogInspetor.enable();
					final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
					
					MasterAEnt[] detailACompIdMasterAEntArr = new MasterAEnt[3];
					int[] detailACompIdMasterAEntSubIdArr = new int[]{0, 0, 0};
					MasterBEnt[] detailAComponentMasterBEntArr = new MasterBEnt[3];
					List objectsToSaveList = new ArrayList<>();
					for (int i = 0; i < 10; i++) {
						MasterAEnt masterAEnt = new MasterAEnt();
						masterAEnt.setId(i);				
						masterAEnt.setVcharA(MessageFormat.format("MasterAEnt_REG{0,number,00}_REG01_VcharA", i));
						masterAEnt.setVcharB(MessageFormat.format("MasterAEnt_REG{0,number,00}_REG01_VcharB", i));
						masterAEnt.setDateA(df.parse(MessageFormat.format("2019-{0,number,00}-{0,number,00} 00:00:00.00000", i)));
						masterAEnt.setDatetimeA(df.parse(MessageFormat.format("2019-01-01 01:{0,number,00}:{0,number,00}", i) + ".00000"));
//						System.out.println("####: " + masterAEnt.getDatetimeA());
//						System.out.println("####: " + masterAEnt.getDatetimeA().getTime());
						masterAEnt.setBlobA(MessageFormat.format("MasterAEnt_REG{0,number,00}_BlobA", i).getBytes(StandardCharsets.UTF_8));
						masterAEnt.setDetailAEntCol(new LinkedHashSet<>());
						masterAEnt.setBlobB(PlayerManagerTest.this.hibernateJpaCompat.getConnection(ss, null).createBlob());
						OutputStream os = masterAEnt.getBlobB().setBinaryStream(1);
						os.write(MessageFormat.format("MasterAEnt_REG{0,number,00}_BlobB", i).getBytes(StandardCharsets.UTF_8));
						os.flush();
						os.close();
						
						masterAEnt.setBlobLazyA(MessageFormat.format("MasterAEnt_REG{0,number,00}_BlobLazyA", i).getBytes(StandardCharsets.UTF_8));
						masterAEnt.setBlobLazyB(PlayerManagerTest.this.hibernateJpaCompat.getConnection(ss, null).createBlob());
						os = masterAEnt.getBlobLazyB().setBinaryStream(1);
						os.write(MessageFormat.format("MasterAEnt_REG{0,number,00}_BlobLazyB", i).getBytes(StandardCharsets.UTF_8));
						os.flush();
						os.close();
						
						masterAEnt.setClobLazyA(MessageFormat.format("MasterAEnt_REG{0,number,00}_ClobLazyB", i));
						masterAEnt.setClobLazyB(PlayerManagerTest.this.hibernateJpaCompat.getConnection(ss, null).createClob());
						Writer w = masterAEnt.getClobLazyB().setCharacterStream(1);
						w.write(MessageFormat.format("MasterAEnt_REG{0,number,00}_ClobLazyB", i));
						w.flush();
						w.close();
						
						//ss.save(masterAEnt);
						objectsToSaveList.add(masterAEnt);
						if (i < detailACompIdMasterAEntArr.length) {
							detailACompIdMasterAEntArr[i] = masterAEnt;
						}
					}
					
					for (int i = 0; i < 10; i++) {
						MasterBEnt masterBEnt = new MasterBEnt();
						MasterBCompId compId = new MasterBCompId();
						compId.setIdA(i);
						compId.setIdB(i);
						masterBEnt.setCompId(compId);				
						masterBEnt.setVcharA(MessageFormat.format("MasterBEnt_REG{0,number,00}_REG01_VcharA", i));
						masterBEnt.setVcharB(MessageFormat.format("MasterBEnt_REG{0,number,00}_REG01_VcharB", i));
						masterBEnt.setDateA(df.parse(MessageFormat.format("2019-{0,number,00}-{0,number,00} 00:00:00.00000", i)));
						masterBEnt.setDatetimeA(df.parse(MessageFormat.format("2019-01-01 01:{0,number,00}:{0,number,00}", i) + ".00000"));
						masterBEnt.setBlobA(MessageFormat.format("MasterBEnt_REG{0,number,00}_BlobA", i).getBytes(StandardCharsets.UTF_8));
						masterBEnt.setBlobB(PlayerManagerTest.this.hibernateJpaCompat.getConnection(ss, null).createBlob());
						masterBEnt.setDetailAEntCol(new LinkedHashSet<>());
						OutputStream os = masterBEnt.getBlobB().setBinaryStream(1);
						os.write(MessageFormat.format("MasterBEnt_REG{0,number,00}_BlobB", i).getBytes(StandardCharsets.UTF_8));
						os.flush();
						os.close();
						//ss.save(masterBEnt);
						objectsToSaveList.add(masterBEnt);
						if (i < detailAComponentMasterBEntArr.length) {
							detailAComponentMasterBEntArr[i] = masterBEnt;
						}
					}
					for (int i = 0; i < 10; i++) {
						DetailAEnt detailAEnt = new DetailAEnt();
						DetailACompId compId = new DetailACompId();
						DetailAComp component = new DetailAComp();
						int detailACompIdMasterAEntIndex = i % detailACompIdMasterAEntArr.length;
						int detailAComponentMasterBEntIndex = i % detailAComponentMasterBEntArr.length;
						compId.setMasterA(detailACompIdMasterAEntArr[detailACompIdMasterAEntIndex]);
						compId.setSubId(detailACompIdMasterAEntSubIdArr[detailACompIdMasterAEntIndex]++);
						detailACompIdMasterAEntArr[detailACompIdMasterAEntIndex].getDetailAEntCol().add(detailAEnt);
						detailAEnt.setCompId(compId);				
						component.setVcharA(MessageFormat.format("DetailAEnt_REG{0,number,00}_REG01_VcharA", i));
						component.setVcharB(MessageFormat.format("DetailAEnt_REG{0,number,00}_REG01_VcharB", i));
						component.setBlobA(MessageFormat.format("DetailAEnt_REG{0,number,00}_BlobA", i).getBytes(StandardCharsets.UTF_8));
						component.setBlobB(PlayerManagerTest.this.hibernateJpaCompat.getConnection(ss, null).createBlob());
						component.setMasterB(detailAComponentMasterBEntArr[detailAComponentMasterBEntIndex]);
						detailAEnt.setDetailAComp(component);
						detailAComponentMasterBEntArr[detailAComponentMasterBEntIndex].getDetailAEntCol().add(detailAEnt);
						OutputStream os = component.getBlobB().setBinaryStream(1);
						os.write(MessageFormat.format("DetailAEnt_REG{0,number,00}_BlobB", i).getBytes(StandardCharsets.UTF_8));
						os.flush();
						os.close();
						//ss.save(detailAEnt);
					}
					
					for (Object itemToSave : objectsToSaveList) {
						ss.save(itemToSave);
					}
					
					//sqlLogInspetor.disable();
				} catch (Exception e) {
					throw new RuntimeException("Unexpected", e);
				} finally {
					//tx.commit();
					ss.flush();
				}
				
				return null;
			}
			
		});
    }

    public void setUpCustom(int  masterCount) throws Exception {
    	this.createDataBaseStructures();
		
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(TransactionStatus status) {
				int bigLoopCount = (int) Math.floor((double)(masterCount - 1) / (double)10);
				
				Session ss = PlayerManagerTest.this.sessionFactory.getCurrentSession();
				try {
					
					//SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
					//sqlLogInspetor.enable();
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
					List objectsToSaveList = new ArrayList<>();
					for (int iBigLoop = 0; iBigLoop < (bigLoopCount + 1); iBigLoop++) {
						int iBigLoopIncremment = iBigLoop * 10;
						MasterAEnt[] detailACompIdMasterAEntArr = new MasterAEnt[3];
						int[] detailACompIdMasterAEntSubIdArr = new int[]{0, 0, 0};
						MasterBEnt[] detailAComponentMasterBEntArr = new MasterBEnt[3];
						for (int i = 0; i < 10; i++) {
							MasterAEnt masterAEnt = new MasterAEnt();
							masterAEnt.setId(i + iBigLoopIncremment);				
							masterAEnt.setVcharA(MessageFormat.format("MasterAEnt_REG{0,number,00}_REG01_VcharA", i + iBigLoopIncremment));
							masterAEnt.setVcharB(MessageFormat.format("MasterAEnt_REG{0,number,00}_REG01_VcharB", i + iBigLoopIncremment));
							masterAEnt.setDateA(df.parse(MessageFormat.format("2019-{0,number,00}-{0,number,00} 00:00:00.00000", i)));
							masterAEnt.setDatetimeA(df.parse(MessageFormat.format("2019-01-01 01:{0,number,00}:{0,number,00}", i) + ".00000"));
//							System.out.println("####: " + masterAEnt.getDatetimeA());
//							System.out.println("####: " + masterAEnt.getDatetimeA().getTime());
							masterAEnt.setBlobA(MessageFormat.format("MasterAEnt_REG{0,number,00}_BlobA", i).getBytes(StandardCharsets.UTF_8));
							masterAEnt.setDetailAEntCol(new LinkedHashSet<>());
							masterAEnt.setBlobB(PlayerManagerTest.this.hibernateJpaCompat.getConnection(ss, null).createBlob());
							OutputStream os = masterAEnt.getBlobB().setBinaryStream(1);
							os.write(MessageFormat.format("MasterAEnt_REG{0,number,00}_BlobB", i + iBigLoopIncremment).getBytes(StandardCharsets.UTF_8));
							os.flush();
							os.close();
							
							masterAEnt.setBlobLazyA(MessageFormat.format("MasterAEnt_REG{0,number,00}_BlobLazyA", i + iBigLoopIncremment).getBytes(StandardCharsets.UTF_8));
							masterAEnt.setBlobLazyB(PlayerManagerTest.this.hibernateJpaCompat.getConnection(ss, null).createBlob());
							os = masterAEnt.getBlobLazyB().setBinaryStream(1);
							os.write(MessageFormat.format("MasterAEnt_REG{0,number,00}_BlobLazyB", i + iBigLoopIncremment).getBytes(StandardCharsets.UTF_8));
							os.flush();
							os.close();
							
							masterAEnt.setClobLazyA(MessageFormat.format("MasterAEnt_REG{0,number,00}_ClobLazyB", i + iBigLoopIncremment));
							masterAEnt.setClobLazyB(PlayerManagerTest.this.hibernateJpaCompat.getConnection(ss, null).createClob());
							Writer w = masterAEnt.getClobLazyB().setCharacterStream(1);
							w.write(MessageFormat.format("MasterAEnt_REG{0,number,00}_ClobLazyB", i));
							w.flush();
							w.close();
							
							//ss.save(masterAEnt);
							objectsToSaveList.add(masterAEnt);
							if (i < detailACompIdMasterAEntArr.length) {
								detailACompIdMasterAEntArr[i] = masterAEnt;
							}
						}
						
						for (int i = 0; i < 10; i++) {
							MasterBEnt masterBEnt = new MasterBEnt();
							MasterBCompId compId = new MasterBCompId();
							compId.setIdA(1 + iBigLoopIncremment);
							compId.setIdB(i);
							masterBEnt.setCompId(compId);				
							masterBEnt.setVcharA(MessageFormat.format("MasterBEnt_REG{0,number,00}_REG01_VcharA", i + iBigLoopIncremment));
							masterBEnt.setVcharB(MessageFormat.format("MasterBEnt_REG{0,number,00}_REG01_VcharB", i + iBigLoopIncremment));
							masterBEnt.setDateA(df.parse(MessageFormat.format("2019-{0,number,00}-{0,number,00} 00:00:00.00000", i)));
							masterBEnt.setDatetimeA(df.parse(MessageFormat.format("2019-01-01 01:{0,number,00}:{0,number,00}", i) + ".00000"));
							masterBEnt.setBlobA(MessageFormat.format("MasterBEnt_REG{0,number,00}_BlobA", i + iBigLoopIncremment).getBytes(StandardCharsets.UTF_8));
							masterBEnt.setBlobB(PlayerManagerTest.this.hibernateJpaCompat.getConnection(ss, null).createBlob());
							masterBEnt.setDetailAEntCol(new LinkedHashSet<>());
							OutputStream os = masterBEnt.getBlobB().setBinaryStream(1);
							os.write(MessageFormat.format("MasterBEnt_REG{0,number,00}_BlobB", i + iBigLoopIncremment).getBytes(StandardCharsets.UTF_8));
							os.flush();
							os.close();
							//ss.save(masterBEnt);
							objectsToSaveList.add(masterBEnt);
							if (i < detailAComponentMasterBEntArr.length) {
								detailAComponentMasterBEntArr[i] = masterBEnt;
							}
						}
						for (int i = 0; i < 10; i++) {
							DetailAEnt detailAEnt = new DetailAEnt();
							DetailACompId compId = new DetailACompId();
							DetailAComp component = new DetailAComp();
							int detailACompIdMasterAEntIndex = i % detailACompIdMasterAEntArr.length;
							int detailAComponentMasterBEntIndex = i % detailAComponentMasterBEntArr.length;
							compId.setMasterA(detailACompIdMasterAEntArr[detailACompIdMasterAEntIndex]);
							compId.setSubId(detailACompIdMasterAEntSubIdArr[detailACompIdMasterAEntIndex]++);
							detailACompIdMasterAEntArr[detailACompIdMasterAEntIndex].getDetailAEntCol().add(detailAEnt);
							detailAEnt.setCompId(compId);				
							component.setVcharA(MessageFormat.format("DetailAEnt_REG{0,number,00}_REG01_VcharA", i + iBigLoopIncremment));
							component.setVcharB(MessageFormat.format("DetailAEnt_REG{0,number,00}_REG01_VcharB", i + iBigLoopIncremment));
							component.setBlobA(MessageFormat.format("DetailAEnt_REG{0,number,00}_BlobA", i + iBigLoopIncremment).getBytes(StandardCharsets.UTF_8));
							component.setBlobB(PlayerManagerTest.this.hibernateJpaCompat.getConnection(ss, null).createBlob());
							component.setMasterB(detailAComponentMasterBEntArr[detailAComponentMasterBEntIndex]);
							detailAEnt.setDetailAComp(component);
							detailAComponentMasterBEntArr[detailAComponentMasterBEntIndex].getDetailAEntCol().add(detailAEnt);
							OutputStream os = component.getBlobB().setBinaryStream(1);
							os.write(MessageFormat.format("DetailAEnt_REG{0,number,00}_BlobB", i + iBigLoopIncremment).getBytes(StandardCharsets.UTF_8));
							os.flush();
							os.close();
							//ss.save(detailAEnt);
						}
						
						for (Object itemToSave : objectsToSaveList) {
							ss.save(itemToSave);
						}
					}
					
					//sqlLogInspetor.disable();
				} catch (Exception e) {
					throw new RuntimeException("Unexpected", e);
				} finally {
					ss.flush();
				}
				return null;
			}
		});
    }

    
    @After
    public void tearDown() throws Exception {
    }
    
    @Autowired
    ApplicationContext applicationContext;
    
//    @Autowired(required=false)
//    @Qualifier("&localSessionFactoryBean3")
//    private Object localSessionFactoryBean;
////    private LocalSessionFactoryBean localSessionFactoryBean;
    private Object getLocalSessionFactoryBean3() {
    	if (this.applicationContext.containsBean("&localSessionFactoryBean3")) {
    		return this.applicationContext.getBean("&localSessionFactoryBean3");    		
    	} else {
    		return null;
    	}
    }
    
//    //@Autowired(required=false)
//    @Autowired
//    @Qualifier("&localSessionFactoryBean4")
//    private Object localSessionFactoryBean4;
//    //private org.springframework.orm.hibernate4.LocalSessionFactoryBean localSessionFactoryBean4;
    private Object getLocalSessionFactoryBean4() {
    	if (this.applicationContext.containsBean("&localSessionFactoryBean4")) {
    		return this.applicationContext.getBean("&localSessionFactoryBean4");    		
    	} else {
    		return null;
    	}
    }
    
//    @Autowired(required=false)
//    @Qualifier("&localSessionFactoryBean5")
//    private org.springframework.orm.hibernate5.LocalSessionFactoryBean localSessionFactoryBean5;
    private Object getLocalSessionFactoryBean5OrJpa() {
    	if (this.applicationContext.containsBean("&localSessionFactoryBean5")) {
    		return this.applicationContext.getBean("&localSessionFactoryBean5");    		
    	} else if (this.applicationContext.containsBean("&localSessionFactoryBeanJpa")) {
    		return this.applicationContext.getBean("&localSessionFactoryBeanJpa");    		
    	} else {
    		return null;
    	}
    }
    
    
    @Autowired
    PlatformTransactionManager transactionManager;
    
	@Test
	public void masterATest() throws Exception {		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".masterATest_result_generated.json";
		
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		PlayerManagerTest.this.manager.startJsonWriteIntersept();
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				MasterAEnt masterAEnt = (MasterAEnt) ss.get(MasterAEnt.class, 1);
				System.out.println("$$$$: " + masterAEnt.getDatetimeA());
				System.out.println("$$$$: " + masterAEnt.getDatetimeA().getTime());
			
				PlayerManagerTest
					.this
						.manager
						.overwriteConfigurationTemporarily(
							PlayerManagerTest
								.this
									.manager
										.getConfig()
											.clone()
											.configSerialiseBySignatureAllRelationship(true));
				
				PlayerSnapshot<MasterAEnt> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAEnt);
				
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, playerSnapshot);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException("Unexpected", e);
				}
				
				sqlLogInspetor.disable();
				
				return null;
			}
			
		});
		PlayerManagerTest.this.manager.stopJsonWriteIntersept();
		
		ClassLoader classLoader = getClass().getClassLoader();
		BufferedReader brExpected = 
			new BufferedReader(
				new InputStreamReader(
					classLoader.getResourceAsStream("jsonplayback/"+PlayerManagerTest.class.getName()+".masterATest_result_expected.json")
				)
			);
		BufferedReader brGenerated = 
			new BufferedReader(
				new InputStreamReader(
					new FileInputStream(generatedFileResult)
				)
			);
		
		String strLineExpected;
		String strLineGenerated;
		int lineCount = 1;
		while ((strLineExpected = brExpected.readLine()) != null)   {
			strLineExpected = strLineExpected.trim();
			strLineGenerated = brGenerated.readLine();
			if (strLineGenerated != null) {
				strLineGenerated = strLineGenerated.trim();
			}
			Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
		}
	}
	
	@Test
	public void masterABlobLazyBNullTest() throws Exception {		
		try {
			Session ss = this.sessionFactory.openSession();
			String generatedFileResult = "target/" + PlayerManagerTest.class.getName()
					+ ".masterABlobLazyBNullTest_result_generated.json";

			TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
			PlayerManagerTest.this.manager.startJsonWriteIntersept();
			transactionTemplate.execute(new TransactionCallback<Object>() {

				@Override
				public Object doInTransaction(TransactionStatus arg0) {
					// SchemaExport

					// Configuration hbConfiguration =
					// PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();

					SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
					sqlLogInspetor.enable();

					MasterAEnt masterAEnt = (MasterAEnt) ss.get(MasterAEnt.class, 1);
					masterAEnt.setBlobLazyB(null);
					arg0.flush();
					sqlLogInspetor.disable();
					return null;
				}
			});

			transactionTemplate = new TransactionTemplate(this.transactionManager);
			transactionTemplate.execute(new TransactionCallback<Object>() {

				@Override
				public Object doInTransaction(TransactionStatus arg0) {
					// SchemaExport

					// Configuration hbConfiguration =
					// PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();

					SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
					sqlLogInspetor.enable();

					MasterAEnt masterAEnt = (MasterAEnt) ss.get(MasterAEnt.class, 1);
					System.out.println("$$$$: " + masterAEnt.getDatetimeA());
					System.out.println("$$$$: " + masterAEnt.getDatetimeA().getTime());

					PlayerManagerTest.this.manager.overwriteConfigurationTemporarily(PlayerManagerTest.this.manager
							.getConfig().clone().configSerialiseBySignatureAllRelationship(true));

					PlayerSnapshot<MasterAEnt> playerSnapshot = PlayerManagerTest.this.manager
							.createPlayerSnapshot(masterAEnt);

					FileOutputStream fos;
					try {
						fos = new FileOutputStream(generatedFileResult);
						PlayerManagerTest.this.manager.getConfig().getObjectMapper().writerWithDefaultPrettyPrinter()
								.writeValue(fos, playerSnapshot);

					} catch (Exception e) {
						// TODO Auto-generated catch block
						throw new RuntimeException("Unexpected", e);
					}

					sqlLogInspetor.disable();

					return null;
				}

			});
			PlayerManagerTest.this.manager.stopJsonWriteIntersept();

			ClassLoader classLoader = getClass().getClassLoader();
			BufferedReader brExpected = new BufferedReader(
					new InputStreamReader(classLoader.getResourceAsStream("jsonplayback/"
							+ PlayerManagerTest.class.getName() + ".masterABlobLazyBNullTest_result_expected.json")));
			BufferedReader brGenerated = new BufferedReader(
					new InputStreamReader(new FileInputStream(generatedFileResult)));

			String strLineExpected;
			String strLineGenerated;
			int lineCount = 1;
			while ((strLineExpected = brExpected.readLine()) != null) {
				strLineExpected = strLineExpected.trim();
				strLineGenerated = brGenerated.readLine();
				if (strLineGenerated != null) {
					strLineGenerated = strLineGenerated.trim();
				}
				Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
			}
		} finally {
			// resetting database
			this.setUp();
		}
	}
	
	
	@Test
	public void masterAList1000Test() throws Exception {		
		try {
			this.setUpCustom(1000);
			Session ss = this.sessionFactory.openSession();
			String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".masterAList1000Test_result_generated.json";
			TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
			PlayerManagerTest.this.manager.startJsonWriteIntersept();
			ObjPersistenceSupport objPersistenceSupport = ((IPlayerManagerImplementor)this.manager).getObjPersistenceSupport();
			transactionTemplate.execute(new TransactionCallback<Object>() {
				
				@Override
				public Object doInTransaction(TransactionStatus arg0) {
					//SchemaExport
					
					//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
					
					SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
					sqlLogInspetor.enable();
					
					@SuppressWarnings("unchecked")
					List<MasterAEnt> masterAEntList = PlayerManagerTest.this.hibernateJpaCompat.createCriteria(ss, MasterAEnt.class)
						.addOrder(OrderCompat.asc("id")).list();
					
					PlayerManagerTest.this.manager
						.overwriteConfigurationTemporarily(
							PlayerManagerTest.this.manager.getConfig().clone()
								.configSerialiseBySignatureAllRelationship(true));
					
					PlayerSnapshot<List<MasterAEnt>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAEntList);
					
					FileOutputStream fos;
					try {
						fos = new FileOutputStream(generatedFileResult);
						PlayerManagerTest
						.this
						.manager
						.getConfig()
						.getObjectMapper()
						.writerWithDefaultPrettyPrinter()
						.writeValue(fos, playerSnapshot);
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						throw new RuntimeException("Unexpected", e);
					}
					
					sqlLogInspetor.disable();
					
					return null;
				}
				
			});
			PlayerManagerTest.this.manager.stopJsonWriteIntersept();
			
			ClassLoader classLoader = getClass().getClassLoader();
			BufferedReader brExpected = 
					new BufferedReader(
							new InputStreamReader(
									classLoader.getResourceAsStream("jsonplayback/"+PlayerManagerTest.class.getName()+".masterAList1000Test_result_expected.json")
									)
							);
			BufferedReader brGenerated = 
					new BufferedReader(
							new InputStreamReader(
									new FileInputStream(generatedFileResult)
									)
							);
			
			String strLineExpected;
			String strLineGenerated;
			int lineCount = 1;
			while ((strLineExpected = brExpected.readLine()) != null)   {
				strLineExpected = strLineExpected.trim();
				strLineGenerated = brGenerated.readLine();
				if (strLineGenerated != null) {
					strLineGenerated = strLineGenerated.trim();
				}
				Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
			}
		} finally {
			//resetting database
			this.setUp();
		}
	}
	
	@Test
	public void masterAListFirstTwiceTest() throws Exception {
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".masterAListFirstTwiceTest_result_generated.json";
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		PlayerManagerTest.this.manager.startJsonWriteIntersept();
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				MasterAEnt masterAEnt = (MasterAEnt) ss.get(MasterAEnt.class, 1);
				List<MasterAEnt> masterAEntList = new ArrayList<>();
				masterAEntList.add(masterAEnt);
				masterAEntList.add(masterAEnt);				
				PlayerManagerTest
					.this
						.manager
						.overwriteConfigurationTemporarily(
							PlayerManagerTest
								.this
									.manager
										.getConfig()
											.clone()
											.configSerialiseBySignatureAllRelationship(true));
				
				PlayerSnapshot<List<MasterAEnt>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAEntList);
				
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, playerSnapshot);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException("Unexpected", e);
				}
				
				sqlLogInspetor.disable();
				
				return null;
			}
			
		});
		PlayerManagerTest.this.manager.stopJsonWriteIntersept();
		
		ClassLoader classLoader = getClass().getClassLoader();
		BufferedReader brExpected = 
			new BufferedReader(
				new InputStreamReader(
					classLoader.getResourceAsStream("jsonplayback/"+PlayerManagerTest.class.getName()+".masterAListFirstTwiceTest_result_expected.json")
				)
			);
		BufferedReader brGenerated = 
			new BufferedReader(
				new InputStreamReader(
					new FileInputStream(generatedFileResult)
				)
			);
		
		String strLineExpected;
		String strLineGenerated;
		int lineCount = 1;
		while ((strLineExpected = brExpected.readLine()) != null)   {
			strLineExpected = strLineExpected.trim();
			strLineGenerated = brGenerated.readLine();
			if (strLineGenerated != null) {
				strLineGenerated = strLineGenerated.trim();
			}
			Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
		}
	}
	
	@Test
	public void masterBList10Test() throws Exception {		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".masterBList10Test_result_generated.json";
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				@SuppressWarnings("unchecked")
				List<MasterBEnt> masterBEntList = 
						PlayerManagerTest.this.hibernateJpaCompat.createCriteria(ss, MasterBEnt.class)
							.addOrder(OrderCompat.asc("compId.idA"))
							.addOrder(OrderCompat.asc("compId.idB")).list();
				
				PlayerManagerTest
					.this
						.manager
						.overwriteConfigurationTemporarily(
							PlayerManagerTest
								.this
									.manager
										.getConfig()
											.clone()
											.configSerialiseBySignatureAllRelationship(false));
				
				PlayerSnapshot<List<MasterBEnt>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterBEntList);
				
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, playerSnapshot);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException("Unexpected", e);
				}
				
				sqlLogInspetor.disable();
				
				return null;
			}
			
		});
		PlayerManagerTest.this.manager.stopJsonWriteIntersept();
		
		ClassLoader classLoader = getClass().getClassLoader();
		BufferedReader brExpected = 
			new BufferedReader(
				new InputStreamReader(
					classLoader.getResourceAsStream("jsonplayback/"+PlayerManagerTest.class.getName()+".masterBList10Test_result_expected.json")
				)
			);
		BufferedReader brGenerated = 
			new BufferedReader(
				new InputStreamReader(
					new FileInputStream(generatedFileResult)
				)
			);
		
		String strLineExpected;
		String strLineGenerated;
		int lineCount = 1;
		while ((strLineExpected = brExpected.readLine()) != null)   {
			strLineExpected = strLineExpected.trim();
			strLineGenerated = brGenerated.readLine();
			if (strLineGenerated != null) {
				strLineGenerated = strLineGenerated.trim();
			}
			Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
		}
	}
	
	@Test
	public void detailACompIdList10Test() throws Exception {		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".detailACompIdList10Test_result_generated.json";
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		PlayerManagerTest.this.manager.startJsonWriteIntersept();
		ObjPersistenceSupport objPersistenceSupport = ((IPlayerManagerImplementor)this.manager).getObjPersistenceSupport();
		
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				@SuppressWarnings("unchecked")
				List<DetailAEnt> detailAEntList = 
						PlayerManagerTest.this.hibernateJpaCompat.createCriteria(ss, DetailAEnt.class)
							.addOrder(OrderCompat.asc("compId.masterA.id"))
							.addOrder(OrderCompat.asc("compId.subId")).list();
				
				List<DetailACompId> detailACompIdList = new ArrayList<>();
				for (DetailAEnt detailAEnt : detailAEntList) {
					detailACompIdList.add(detailAEnt.getCompId());
					PlayerManagerTest.this.manager.registerComponentOwner(detailAEnt, d -> d.getCompId());
				}
				
				PlayerManagerTest
					.this
						.manager
						.overwriteConfigurationTemporarily(
							PlayerManagerTest
								.this
									.manager
										.getConfig()
											.clone()
											.configSerialiseBySignatureAllRelationship(false));
				
				PlayerSnapshot<List<DetailACompId>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailACompIdList);
				
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, playerSnapshot);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException("Unexpected", e);
				}
				
				sqlLogInspetor.disable();
				
				return null;
			}
			
		});
		PlayerManagerTest.this.manager.stopJsonWriteIntersept();
		
		ClassLoader classLoader = getClass().getClassLoader();
		BufferedReader brExpected = 
			new BufferedReader(
				new InputStreamReader(
					classLoader.getResourceAsStream("jsonplayback/"+PlayerManagerTest.class.getName()+".detailACompIdList10Test_result_expected.json")
				)
			);
		BufferedReader brGenerated = 
			new BufferedReader(
				new InputStreamReader(
					new FileInputStream(generatedFileResult)
				)
			);
		
		String strLineExpected;
		String strLineGenerated;
		int lineCount = 1;
		while ((strLineExpected = brExpected.readLine()) != null)   {
			strLineExpected = strLineExpected.trim();
			strLineGenerated = brGenerated.readLine();
			if (strLineGenerated != null) {
				strLineGenerated = strLineGenerated.trim();
			}
			Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
		}
	}
	
	@Test
	public void detailACompIdListDummyOwner10Test() throws Exception {		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".detailACompIdListDummyOwner10Test_result_generated.json";
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		PlayerManagerTest.this.manager.startJsonWriteIntersept();
		ObjPersistenceSupport objPersistenceSupport = ((IPlayerManagerImplementor)this.manager).getObjPersistenceSupport();
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				@SuppressWarnings("unchecked")
				List<DetailAEnt> detailAEntList = 
						PlayerManagerTest.this.hibernateJpaCompat.createCriteria(ss, DetailAEnt.class)
							.addOrder(OrderCompat.asc("compId.masterA.id"))
							.addOrder(OrderCompat.asc("compId.subId")).list();
				
				List<DetailACompId> detailACompIdList = new ArrayList<>();
				for (DetailAEnt detailAEnt : detailAEntList) {
					detailACompIdList.add(detailAEnt.getCompId());
					PlayerManagerTest.this.manager.registerComponentOwner(DetailAEnt.class, detailAEnt.getCompId(), d -> d.getCompId());
				}
				
				PlayerManagerTest
					.this
						.manager
						.overwriteConfigurationTemporarily(
							PlayerManagerTest
								.this
									.manager
										.getConfig()
											.clone()
											.configSerialiseBySignatureAllRelationship(false));
				
				PlayerSnapshot<List<DetailACompId>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailACompIdList);
				
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, playerSnapshot);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException("Unexpected", e);
				}
				
				sqlLogInspetor.disable();
				
				return null;
			}
			
		});
		PlayerManagerTest.this.manager.stopJsonWriteIntersept();
		
		ClassLoader classLoader = getClass().getClassLoader();
		BufferedReader brExpected = 
			new BufferedReader(
				new InputStreamReader(
					classLoader.getResourceAsStream("jsonplayback/"+PlayerManagerTest.class.getName()+".detailACompIdListDummyOwner10Test_result_expected.json")
				)
			);
		BufferedReader brGenerated = 
			new BufferedReader(
				new InputStreamReader(
					new FileInputStream(generatedFileResult)
				)
			);
		
		String strLineExpected;
		String strLineGenerated;
		int lineCount = 1;
		while ((strLineExpected = brExpected.readLine()) != null)   {
			strLineExpected = strLineExpected.trim();
			strLineGenerated = brGenerated.readLine();
			if (strLineGenerated != null) {
				strLineGenerated = strLineGenerated.trim();
			}
			Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
		}
	}
	
	
	@Test
	public void detailACompCompListDummyOwner10Test() throws Exception {		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".detailACompCompListDummyOwner10Test_result_generated.json";
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		PlayerManagerTest.this.manager.startJsonWriteIntersept();
		ObjPersistenceSupport objPersistenceSupport = ((IPlayerManagerImplementor)this.manager).getObjPersistenceSupport();
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				@SuppressWarnings("unchecked")
				List<DetailAEnt> detailAEntList = 
						PlayerManagerTest.this.hibernateJpaCompat.createCriteria(ss, DetailAEnt.class)
							.addOrder(OrderCompat.asc("compId.masterA.id"))
							.addOrder(OrderCompat.asc("compId.subId")).list();
				
				List<DetailACompComp> detailACompIdList = new ArrayList<>();
				for (DetailAEnt detailAEnt : detailAEntList) {
					detailACompIdList.add(detailAEnt.getDetailAComp().getDetailACompComp());
					PlayerManagerTest.this.manager.registerComponentOwner(
							DetailAEnt.class, 
							detailAEnt.getDetailAComp().getDetailACompComp(),
							d -> d.getDetailAComp().getDetailACompComp());
				}
				
				PlayerManagerTest
					.this
						.manager
						.overwriteConfigurationTemporarily(
							PlayerManagerTest
								.this
									.manager
										.getConfig()
											.clone()
											.configSerialiseBySignatureAllRelationship(false));
				
				PlayerSnapshot<List<DetailACompComp>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailACompIdList);
				
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, playerSnapshot);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException("Unexpected", e);
				}
				
				sqlLogInspetor.disable();
				
				return null;
			}
			
		});
		PlayerManagerTest.this.manager.stopJsonWriteIntersept();
		
		ClassLoader classLoader = getClass().getClassLoader();
		BufferedReader brExpected = 
			new BufferedReader(
				new InputStreamReader(
					classLoader.getResourceAsStream("jsonplayback/"+PlayerManagerTest.class.getName()+".detailACompCompListDummyOwner10Test_result_expected.json")
				)
			);
		BufferedReader brGenerated = 
			new BufferedReader(
				new InputStreamReader(
					new FileInputStream(generatedFileResult)
				)
			);
		
		String strLineExpected;
		String strLineGenerated;
		int lineCount = 1;
		while ((strLineExpected = brExpected.readLine()) != null)   {
			strLineExpected = strLineExpected.trim();
			strLineGenerated = brGenerated.readLine();
			if (strLineGenerated != null) {
				strLineGenerated = strLineGenerated.trim();
			}
			Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
		}
	}
		
	
	@Test
	public void detailACompCompList10Test() throws Exception {		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".detailACompCompList10Test_result_generated.json";
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		PlayerManagerTest.this.manager.startJsonWriteIntersept();
		ObjPersistenceSupport objPersistenceSupport = ((IPlayerManagerImplementor)this.manager).getObjPersistenceSupport();
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				@SuppressWarnings("unchecked")
				List<DetailAEnt> detailAEntList = 
						PlayerManagerTest.this.hibernateJpaCompat.createCriteria(ss, DetailAEnt.class)
							.addOrder(OrderCompat.asc("compId.masterA.id"))
							.addOrder(OrderCompat.asc("compId.subId")).list();
				
				List<DetailACompComp> detailACompIdList = new ArrayList<>();
				for (DetailAEnt detailAEnt : detailAEntList) {
					detailACompIdList.add(detailAEnt.getDetailAComp().getDetailACompComp());
					PlayerManagerTest.this.manager.registerComponentOwner(detailAEnt, d -> d.getDetailAComp());
					PlayerManagerTest.this.manager.registerComponentOwner(detailAEnt.getDetailAComp(), dc -> dc.getDetailACompComp());
				}
				
				PlayerManagerTest
					.this
						.manager
						.overwriteConfigurationTemporarily(
							PlayerManagerTest
								.this
									.manager
										.getConfig()
											.clone()
											.configSerialiseBySignatureAllRelationship(false));
				
				PlayerSnapshot<List<DetailACompComp>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailACompIdList);
				
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, playerSnapshot);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException("Unexpected", e);
				}
				
				sqlLogInspetor.disable();
				
				return null;
			}
			
		});
		PlayerManagerTest.this.manager.stopJsonWriteIntersept();
		
		ClassLoader classLoader = getClass().getClassLoader();
		BufferedReader brExpected = 
			new BufferedReader(
				new InputStreamReader(
					classLoader.getResourceAsStream("jsonplayback/"+PlayerManagerTest.class.getName()+".detailACompCompList10Test_result_expected.json")
				)
			);
		BufferedReader brGenerated = 
			new BufferedReader(
				new InputStreamReader(
					new FileInputStream(generatedFileResult)
				)
			);
		
		String strLineExpected;
		String strLineGenerated;
		int lineCount = 1;
		while ((strLineExpected = brExpected.readLine()) != null)   {
			strLineExpected = strLineExpected.trim();
			strLineGenerated = brGenerated.readLine();
			if (strLineGenerated != null) {
				strLineGenerated = strLineGenerated.trim();
			}
			Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
		}
	}
		
	
	@Test
	public void masterBList10BizarreTest() throws Exception {		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".masterBList10BizarreTest_result_generated.json";
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		PlayerManagerTest.this.manager.startJsonWriteIntersept();
		ObjPersistenceSupport objPersistenceSupport = ((IPlayerManagerImplementor)this.manager).getObjPersistenceSupport();
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				@SuppressWarnings("unchecked")
				List<MasterBEnt> masterBEntList = 
						PlayerManagerTest.this.hibernateJpaCompat.createCriteria(ss, MasterBEnt.class)
							.addOrder(OrderCompat.asc("compId.idA"))
							.addOrder(OrderCompat.asc("compId.idB")).list();
				List<Map<String, Map<String, MasterBEnt>>> masterBEntBizarreList = new ArrayList<>();
				
				for (MasterBEnt masterB : masterBEntList) {
					SignatureBean signBean = ((IPlayerManagerImplementor) PlayerManagerTest.this.manager).generateSignature(masterB);
					String signStr = PlayerManagerTest.this.manager.serializeSignature(signBean);
					Map<String,  Map<String, MasterBEnt>> mapItem = new LinkedHashMap<>();
					PlayerSnapshot<MasterBEnt> masterBPS = PlayerManagerTest.this.manager.createPlayerSnapshot(masterB);
					Map<String, MasterBEnt> mapMapItem = new LinkedHashMap<>();
					mapMapItem.put("wrappedSnapshot", masterB);
					mapItem.put(signStr, mapMapItem);
					masterBEntBizarreList.add(mapItem);
				}
				
				PlayerManagerTest
					.this
						.manager
						.overwriteConfigurationTemporarily(
							PlayerManagerTest
								.this
									.manager
										.getConfig()
											.clone()
											.configSerialiseBySignatureAllRelationship(false));
				
				PlayerSnapshot<List<Map<String, Map<String, MasterBEnt>>>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterBEntBizarreList);
				
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, playerSnapshot);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException("Unexpected", e);
				}
				
				sqlLogInspetor.disable();
				
				return null;
			}
			
		});
		PlayerManagerTest.this.manager.stopJsonWriteIntersept();
		
		ClassLoader classLoader = getClass().getClassLoader();
		BufferedReader brExpected = 
			new BufferedReader(
				new InputStreamReader(
					classLoader.getResourceAsStream("jsonplayback/"+PlayerManagerTest.class.getName()+".masterBList10BizarreTest_result_expected.json")
				)
			);
		BufferedReader brGenerated = 
			new BufferedReader(
				new InputStreamReader(
					new FileInputStream(generatedFileResult)
				)
			);
		
		String strLineExpected;
		String strLineGenerated;
		int lineCount = 1;
		while ((strLineExpected = brExpected.readLine()) != null)   {
			strLineExpected = strLineExpected.trim();
			strLineGenerated = brGenerated.readLine();
			if (strLineGenerated != null) {
				strLineGenerated = strLineGenerated.trim();
			}
			Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
		}
	}
	
	
	
	@Test
	public void masterLazyPrpOverSizedTest() throws Exception {
		try {
			Session ss = this.sessionFactory.openSession();
			String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".masterLazyPrpOverSizedTest_result_generated.json";
			
			TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
			
			PlayerManagerTest.this.manager.startJsonWriteIntersept();
			transactionTemplate.execute(new TransactionCallback<Object>() {
	
				@Override
				public Object doInTransaction(TransactionStatus transactionStatus) {
					try {						
						SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
						sqlLogInspetor.enable();
						
						MasterAEnt masterAEnt = (MasterAEnt) ss.get(MasterAEnt.class, 1);
						byte[] byteArr = masterAEnt.getBlobLazyA();
						ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
						do {
							byteBuffer.put(byteArr);
						} while (byteBuffer.remaining() > byteArr.length);
						byteBuffer.flip();
						masterAEnt.setBlobLazyA(Arrays.copyOf(byteBuffer.array(), byteBuffer.limit()));
	
						byteArr = new byte[(int) masterAEnt.getBlobLazyB().length()];
						masterAEnt.getBlobLazyB().getBinaryStream().read(byteArr, 0, byteArr.length);
						byteBuffer = ByteBuffer.allocate(2048);
						do {
							byteBuffer.put(byteArr);
						} while (byteBuffer.remaining() > byteArr.length);
						byteBuffer.flip();
						masterAEnt.setBlobLazyB(PlayerManagerTest.this.hibernateJpaCompat.getConnection(ss, null).createBlob());
						OutputStream os = masterAEnt.getBlobLazyB().setBinaryStream(1);
						os.write(byteBuffer.array(), 0, byteBuffer.limit());
						os.flush();
						os.close();
						
						CharBuffer cBuffer = CharBuffer.allocate(2048);
						do {
							cBuffer.put(masterAEnt.getClobLazyA());
						} while (cBuffer.remaining() > masterAEnt.getClobLazyA().length());
						cBuffer.flip();
						masterAEnt.setClobLazyA(cBuffer.toString());
						

						char[] charArr = new char[(int) masterAEnt.getClobLazyB().length()];
						masterAEnt.getClobLazyB().getCharacterStream().read(charArr, 0, charArr.length);
						cBuffer = CharBuffer.allocate(2048);
						do {
							cBuffer.put(charArr);
						} while (cBuffer.remaining() > charArr.length);
						cBuffer.flip();
						masterAEnt.setClobLazyB(PlayerManagerTest.this.hibernateJpaCompat.getConnection(ss, null).createClob());
						Writer w = masterAEnt.getClobLazyB().setCharacterStream(1);
						w.write(cBuffer.toString());
						w.flush();
						w.close();
						
						
						sqlLogInspetor.disable();
						
						return null;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				
			});
			PlayerManagerTest.this.manager.stopJsonWriteIntersept();
			
			PlayerManagerTest.this.manager.startJsonWriteIntersept();
			transactionTemplate.execute(new TransactionCallback<Object>() {
	
				@Override
				public Object doInTransaction(TransactionStatus transactionStatus) {
					//SchemaExport
					
					//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
					
					SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
					sqlLogInspetor.enable();
					
					MasterAEnt masterAEnt = (MasterAEnt) ss.get(MasterAEnt.class, 1);
					System.out.println("$$$$: " + masterAEnt.getDatetimeA());
					System.out.println("$$$$: " + masterAEnt.getDatetimeA().getTime());
					
					PlayerManagerTest
						.this
							.manager
							.overwriteConfigurationTemporarily(
								PlayerManagerTest
									.this
										.manager
											.getConfig()
												.clone()
												.configSerialiseBySignatureAllRelationship(true));
					
					PlayerSnapshot<MasterAEnt> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAEnt);
					
					FileOutputStream fos;
					try {
						fos = new FileOutputStream(generatedFileResult);
						PlayerManagerTest
							.this
								.manager
									.getConfig()
										.getObjectMapper()
											.writerWithDefaultPrettyPrinter()
												.writeValue(fos, playerSnapshot);
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						throw new RuntimeException("Unexpected", e);
					}
					
					sqlLogInspetor.disable();
					
					return null;
				}
				
			});
			PlayerManagerTest.this.manager.stopJsonWriteIntersept();
			
			ClassLoader classLoader = getClass().getClassLoader();
			BufferedReader brExpected = 
				new BufferedReader(
					new InputStreamReader(
						classLoader.getResourceAsStream("jsonplayback/"+PlayerManagerTest.class.getName()+".masterLazyPrpOverSizedTest_result_expected.json")
					)
				);
			BufferedReader brGenerated = 
				new BufferedReader(
					new InputStreamReader(
						new FileInputStream(generatedFileResult)
					)
				);
			
			String strLineExpected;
			String strLineGenerated;
			int lineCount = 1;
			while ((strLineExpected = brExpected.readLine()) != null)   {
				strLineExpected = strLineExpected.trim();
				strLineGenerated = brGenerated.readLine();
				if (strLineGenerated != null) {
					strLineGenerated = strLineGenerated.trim();
				}
				Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
			}
		
		} finally {
			//resetting database
			this.setUp();
		}
	}
	
	@Test
	public void masterADetailATest() throws HibernateException, SQLException, JsonGenerationException, JsonMappingException, IOException {
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".masterADetailATest_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		PlayerManagerTest.this.manager.startJsonWriteIntersept();
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				MasterAEnt masterAEnt = (MasterAEnt) ss.get(MasterAEnt.class, 1);
				//doing lazy-load
				masterAEnt.getDetailAEntCol().size();
				
				PlayerManagerTest
					.this
						.manager
						.overwriteConfigurationTemporarily(
							PlayerManagerTest
								.this
									.manager
										.getConfig()
											.clone()
											.configSerialiseBySignatureAllRelationship(false));
				
				PlayerSnapshot<MasterAEnt> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAEnt);
				
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, playerSnapshot);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException("Unexpected", e);
				}
				
				sqlLogInspetor.disable();
				
				return null;
			}
			
		});
		PlayerManagerTest.this.manager.stopJsonWriteIntersept();
		
		ClassLoader classLoader = getClass().getClassLoader();
		BufferedReader brExpected = 
			new BufferedReader(
				new InputStreamReader(
					classLoader.getResourceAsStream("jsonplayback/"+PlayerManagerTest.class.getName()+".masterADetailATest_result_expected.json")
				)
			);
		BufferedReader brGenerated = 
			new BufferedReader(
				new InputStreamReader(
					new FileInputStream(generatedFileResult)
				)
			);
		
		String strLineExpected;
		String strLineGenerated;
		int lineCount = 1;
		while ((strLineExpected = brExpected.readLine()) != null)   {
			strLineExpected = strLineExpected.trim();
			strLineGenerated = brGenerated.readLine();
			if (strLineGenerated != null) {
				strLineGenerated = strLineGenerated.trim();
			}
			Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
		}
	}

	
	@Test
	public void masterAWrapperTest() throws HibernateException, SQLException, JsonGenerationException, JsonMappingException, IOException {
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".masterAWrapperTest_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		PlayerManagerTest.this.manager.startJsonWriteIntersept();
		ObjPersistenceSupport objPersistenceSupport = ((IPlayerManagerImplementor)this.manager).getObjPersistenceSupport();
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				@SuppressWarnings("unchecked")
				List<MasterAEnt> masterAEntList = 
						PlayerManagerTest.this.hibernateJpaCompat.createCriteria(ss, MasterAEnt.class)
							.addOrder(OrderCompat.asc("id")).list();
				List<MasterAWrapper> masterAWrapperList = new ArrayList<>();
				for (MasterAEnt masterAEnt : masterAEntList) {
					MasterAWrapper masterAWrapper = new MasterAWrapper();
					masterAWrapper.setMasterA(masterAEnt);
					masterAWrapper.setDetailAWrapperList(new ArrayList<>());
					masterAWrapper.setDetailAEntCol(new ArrayList<>(masterAEnt.getDetailAEntCol()));
					for (DetailAEnt detailAEnt : masterAEnt.getDetailAEntCol()) {
						DetailAWrapper detailAWrapper = new DetailAWrapper();
						detailAWrapper.setDetailA(detailAEnt);
						masterAWrapper.getDetailAWrapperList().add(detailAWrapper);
					}
					masterAWrapperList.add(masterAWrapper);
				}
				
				PlayerManagerTest
					.this
						.manager
						.overwriteConfigurationTemporarily(
							PlayerManagerTest
								.this
									.manager
										.getConfig()
											.clone()
											.configSerialiseBySignatureAllRelationship(true));
				
				PlayerSnapshot<List<MasterAWrapper>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAWrapperList);
				
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, playerSnapshot);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException("Unexpected", e);
				}
				
				sqlLogInspetor.disable();
				
				return null;
			}
			
		});
		PlayerManagerTest.this.manager.stopJsonWriteIntersept();
		
		ClassLoader classLoader = getClass().getClassLoader();
		BufferedReader brExpected = 
			new BufferedReader(
				new InputStreamReader(
					classLoader.getResourceAsStream("jsonplayback/"+PlayerManagerTest.class.getName()+".masterAWrapperTest_result_expected.json")
				)
			);
		BufferedReader brGenerated = 
			new BufferedReader(
				new InputStreamReader(
					new FileInputStream(generatedFileResult)
				)
			);
		
		String strLineExpected;
		String strLineGenerated;
		int lineCount = 1;
		while ((strLineExpected = brExpected.readLine()) != null)   {
			strLineExpected = strLineExpected.trim();
			strLineGenerated = brGenerated.readLine();
			if (strLineGenerated != null) {
				strLineGenerated = strLineGenerated.trim();
			}
			Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
		}
	}

	@Test
	public void detailAWithoutMasterBTest() throws HibernateException, SQLException, JsonGenerationException, JsonMappingException, IOException {
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".detailAWithoutMasterBTest_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		PlayerManagerTest.this.manager.startJsonWriteIntersept();
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				Session ss = PlayerManagerTest.this.sessionFactory.getCurrentSession();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				MasterAEnt masterAEnt = (MasterAEnt) ss.get(MasterAEnt.class, 1);
				DetailAEnt detailAEnt = new ArrayList<DetailAEnt>(masterAEnt.getDetailAEntCol()).get(0);
				detailAEnt.getDetailAComp().setMasterB(null);
				ss.flush();
				
				PlayerManagerTest
					.this
						.manager
						.overwriteConfigurationTemporarily(
							PlayerManagerTest
								.this
									.manager
										.getConfig()
											.clone()
											.configSerialiseBySignatureAllRelationship(false));
				
				PlayerSnapshot<MasterAEnt> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAEnt);
				
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, playerSnapshot);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException("Unexpected", e);
				}
				
				sqlLogInspetor.disable();
				
				return null;
			}
			
		});
		PlayerManagerTest.this.manager.stopJsonWriteIntersept();
		
		ClassLoader classLoader = getClass().getClassLoader();
		BufferedReader brExpected = 
			new BufferedReader(
				new InputStreamReader(
					classLoader.getResourceAsStream("jsonplayback/"+PlayerManagerTest.class.getName()+".detailAWithoutMasterBTest_result_expected.json")
				)
			);
		BufferedReader brGenerated = 
			new BufferedReader(
				new InputStreamReader(
					new FileInputStream(generatedFileResult)
				)
			);
		
		String strLineExpected;
		String strLineGenerated;
		int lineCount = 1;
		while ((strLineExpected = brExpected.readLine()) != null)   {
			strLineExpected = strLineExpected.trim();
			strLineGenerated = brGenerated.readLine();
			if (strLineGenerated != null) {
				strLineGenerated = strLineGenerated.trim();
			}
			Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
		}
	}

	@Test
	public void detailABySigTest() throws HibernateException, SQLException, JsonGenerationException, JsonMappingException, IOException {
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".detailABySigTest_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		PlayerManagerTest.this.manager.startJsonWriteIntersept();
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				MasterAEnt masterAEnt = (MasterAEnt) ss.get(MasterAEnt.class, 1);
				//doing lazy-load
				masterAEnt.getDetailAEntCol().size();
				
				PlayerManagerTest.this
					.manager
					.overwriteConfigurationTemporarily(
						PlayerManagerTest
							.this
								.manager
									.getConfig()
										.clone()
										.configSerialiseBySignatureAllRelationship(true));
				
				SignatureBean signatureBean = PlayerManagerTest.this.manager.deserializeSignature("eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQUVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoiZGV0YWlsQUVudENvbCIsInJhd0tleVZhbHVlcyI6WyIxIl0sInJhd0tleVR5cGVOYW1lcyI6WyJqYXZhLmxhbmcuSW50ZWdlciJdfQ");
				Collection<DetailAEnt> detailAEntCol = PlayerManagerTest.this.manager.getBySignature(signatureBean);
				PlayerSnapshot<Collection<DetailAEnt>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailAEntCol);
				
				FileOutputStream fos;
				
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, playerSnapshot);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException("Unexpected", e);
				}
				
				sqlLogInspetor.disable();
				
				return null;
			}
			
		});
		PlayerManagerTest.this.manager.stopJsonWriteIntersept();
		
		ClassLoader classLoader = getClass().getClassLoader();
		BufferedReader brExpected = 
			new BufferedReader(
				new InputStreamReader(
					classLoader.getResourceAsStream("jsonplayback/"+PlayerManagerTest.class.getName()+".detailABySigTest_result_expected.json")
				)
			);
		BufferedReader brGenerated = 
			new BufferedReader(
				new InputStreamReader(
					new FileInputStream(generatedFileResult)
				)
			);
		
		String strLineExpected;
		String strLineGenerated;
		int lineCount = 1;
		while ((strLineExpected = brExpected.readLine()) != null)   {
			strLineExpected = strLineExpected.trim();
			strLineGenerated = brGenerated.readLine();
			if (strLineGenerated != null) {
				strLineGenerated = strLineGenerated.trim();
			}
			Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
		}
	}


	@Test
	public void detailAFirstSecontTest() throws HibernateException, SQLException, JsonGenerationException, JsonMappingException, IOException {
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".detailAFirstSecontTest_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		PlayerManagerTest.this.manager.startJsonWriteIntersept();
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				MasterAEnt masterAEnt = (MasterAEnt) ss.get(MasterAEnt.class, 1);
				//doing lazy-load
				masterAEnt.getDetailAEntCol().size();
				
				PlayerManagerTest.this
					.manager
					.overwriteConfigurationTemporarily(
						PlayerManagerTest
							.this
								.manager
									.getConfig()
										.clone()
										.configSerialiseBySignatureAllRelationship(true));
				
				SignatureBean signatureBean = PlayerManagerTest.this.manager.deserializeSignature("eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQUVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoiZGV0YWlsQUVudENvbCIsInJhd0tleVZhbHVlcyI6WyIxIl0sInJhd0tleVR5cGVOYW1lcyI6WyJqYXZhLmxhbmcuSW50ZWdlciJdfQ");
				Collection<DetailAEnt> detailAEntCol = PlayerManagerTest.this.manager.getBySignature(signatureBean);
				ArrayList<DetailAEnt> detailAEntCuttedCol = new ArrayList<>();
				detailAEntCuttedCol.add(new ArrayList<>(detailAEntCol).get(0));
				detailAEntCuttedCol.add(new ArrayList<>(detailAEntCol).get(1));
				PlayerSnapshot<Collection<DetailAEnt>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailAEntCuttedCol);
				
				FileOutputStream fos;
				
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, playerSnapshot);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException("Unexpected", e);
				}
				
				sqlLogInspetor.disable();
				
				return null;
			}
			
		});
		PlayerManagerTest.this.manager.stopJsonWriteIntersept();
		
		ClassLoader classLoader = getClass().getClassLoader();
		BufferedReader brExpected = 
			new BufferedReader(
				new InputStreamReader(
					classLoader.getResourceAsStream("jsonplayback/"+PlayerManagerTest.class.getName()+".detailAFirstSecontTest_result_expected.json")
				)
			);
		BufferedReader brGenerated = 
			new BufferedReader(
				new InputStreamReader(
					new FileInputStream(generatedFileResult)
				)
			);
		
		String strLineExpected;
		String strLineGenerated;
		int lineCount = 1;
		while ((strLineExpected = brExpected.readLine()) != null)   {
			strLineExpected = strLineExpected.trim();
			strLineGenerated = brGenerated.readLine();
			if (strLineGenerated != null) {
				strLineGenerated = strLineGenerated.trim();
			}
			Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
		}
	}

	@Test
	public void detailASecontThirdTest() throws HibernateException, SQLException, JsonGenerationException, JsonMappingException, IOException {
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".detailASecontThirdTest_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		PlayerManagerTest.this.manager.startJsonWriteIntersept();
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				MasterAEnt masterAEnt = (MasterAEnt) ss.get(MasterAEnt.class, 1);
				//doing lazy-load
				masterAEnt.getDetailAEntCol().size();
				
				PlayerManagerTest.this
					.manager
					.overwriteConfigurationTemporarily(
						PlayerManagerTest
							.this
								.manager
									.getConfig()
										.clone()
										.configSerialiseBySignatureAllRelationship(true));
				
				SignatureBean signatureBean = PlayerManagerTest.this.manager.deserializeSignature("eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQUVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoiZGV0YWlsQUVudENvbCIsInJhd0tleVZhbHVlcyI6WyIxIl0sInJhd0tleVR5cGVOYW1lcyI6WyJqYXZhLmxhbmcuSW50ZWdlciJdfQ");
				Collection<DetailAEnt> detailAEntCol = PlayerManagerTest.this.manager.getBySignature(signatureBean);
				ArrayList<DetailAEnt> detailAEntCuttedCol = new ArrayList<>();
				detailAEntCuttedCol.add(new ArrayList<>(detailAEntCol).get(1));
				detailAEntCuttedCol.add(new ArrayList<>(detailAEntCol).get(2));
				PlayerSnapshot<Collection<DetailAEnt>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailAEntCuttedCol);
				
				FileOutputStream fos;
				
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, playerSnapshot);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException("Unexpected", e);
				}
				
				sqlLogInspetor.disable();
				
				return null;
			}
			
		});
		PlayerManagerTest.this.manager.stopJsonWriteIntersept();
		
		ClassLoader classLoader = getClass().getClassLoader();
		BufferedReader brExpected = 
			new BufferedReader(
				new InputStreamReader(
					classLoader.getResourceAsStream("jsonplayback/"+PlayerManagerTest.class.getName()+".detailASecontThirdTest_result_expected.json")
				)
			);
		BufferedReader brGenerated = 
			new BufferedReader(
				new InputStreamReader(
					new FileInputStream(generatedFileResult)
				)
			);
		
		String strLineExpected;
		String strLineGenerated;
		int lineCount = 1;
		while ((strLineExpected = brExpected.readLine()) != null)   {
			strLineExpected = strLineExpected.trim();
			strLineGenerated = brGenerated.readLine();
			if (strLineGenerated != null) {
				strLineGenerated = strLineGenerated.trim();
			}
			Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
		}
	}

	@Test
	public void masterBTest() throws Exception {		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".masterBTest_result_generated.json";
		
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		PlayerManagerTest.this.manager.startJsonWriteIntersept();
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				MasterBCompId compId = new MasterBCompId();
				compId.setIdA(1);
				compId.setIdB(1);
				MasterBEnt masterBEnt = (MasterBEnt) ss.get(MasterBEnt.class, compId);
				System.out.println("$$$$: " + masterBEnt.getDatetimeA());
				System.out.println("$$$$: " + masterBEnt.getDatetimeA().getTime());
			
				PlayerManagerTest
					.this
						.manager
						.overwriteConfigurationTemporarily(
							PlayerManagerTest
								.this
									.manager
										.getConfig()
											.clone()
											.configSerialiseBySignatureAllRelationship(true));
				
				PlayerSnapshot<MasterBEnt> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterBEnt);
				
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, playerSnapshot);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new RuntimeException("Unexpected", e);
				}
				
				sqlLogInspetor.disable();
				
				return null;
			}
			
		});
		PlayerManagerTest.this.manager.stopJsonWriteIntersept();
		
		ClassLoader classLoader = getClass().getClassLoader();
		BufferedReader brExpected = 
			new BufferedReader(
				new InputStreamReader(
					classLoader.getResourceAsStream("jsonplayback/"+PlayerManagerTest.class.getName()+".masterBTest_result_expected.json")
				)
			);
		BufferedReader brGenerated = 
			new BufferedReader(
				new InputStreamReader(
					new FileInputStream(generatedFileResult)
				)
			);
		
		String strLineExpected;
		String strLineGenerated;
		int lineCount = 1;
		while ((strLineExpected = brExpected.readLine()) != null)   {
			strLineExpected = strLineExpected.trim();
			strLineGenerated = brGenerated.readLine();
			if (strLineGenerated != null) {
				strLineGenerated = strLineGenerated.trim();
			}
			Assert.assertThat("Line " + lineCount++, strLineGenerated, equalTo(strLineExpected));
		}
	}
	
	@Test
	public void masterBInnerCompsGetBySigTest() throws Exception {
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getName()+".detailABySigTest_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		PlayerManagerTest.this.manager.startJsonWriteIntersept();
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
								
				SignatureBean signatureBean = PlayerManagerTest.this.manager.deserializeSignature("eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsInJhd0tleVZhbHVlcyI6WyIxIiwiMSJdLCJyYXdLZXlUeXBlTmFtZXMiOlsiamF2YS5sYW5nLkludGVnZXIiLCJqYXZhLmxhbmcuSW50ZWdlciJdfQ");
				MasterBEnt masterBEnt = PlayerManagerTest.this.manager.getBySignature(signatureBean);
				
				signatureBean = PlayerManagerTest.this.manager.deserializeSignature("eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQUVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoiZGV0YWlsQUVudENvbCIsInJhd0tleVZhbHVlcyI6WyIxIl0sInJhd0tleVR5cGVOYW1lcyI6WyJqYXZhLmxhbmcuSW50ZWdlciJdfQ");
				Collection<DetailAEnt> detailAEntCol = PlayerManagerTest.this.manager.getBySignature(signatureBean);
				
				signatureBean = PlayerManagerTest.this.manager.deserializeSignature("eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29tcCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAiLCJyYXdLZXlWYWx1ZXMiOlsiMSIsIjEiXSwicmF3S2V5VHlwZU5hbWVzIjpbImphdmEubGFuZy5JbnRlZ2VyIiwiamF2YS5sYW5nLkludGVnZXIiXX0");
				MasterBComp masterBComp = PlayerManagerTest.this.manager.getBySignature(signatureBean);

				signatureBean = PlayerManagerTest.this.manager.deserializeSignature("eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAuZGV0YWlsQUVudENvbCIsInJhd0tleVZhbHVlcyI6WyIxIiwiMSJdLCJyYXdLZXlUeXBlTmFtZXMiOlsiamF2YS5sYW5nLkludGVnZXIiLCJqYXZhLmxhbmcuSW50ZWdlciJdfQ");
				Collection<DetailAEnt> compDetailAEntCol = PlayerManagerTest.this.manager.getBySignature(signatureBean);

				signatureBean = PlayerManagerTest.this.manager.deserializeSignature("eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29tcCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAubWFzdGVyQkNvbXBDb21wIiwicmF3S2V5VmFsdWVzIjpbIjEiLCIxIl0sInJhd0tleVR5cGVOYW1lcyI6WyJqYXZhLmxhbmcuSW50ZWdlciIsImphdmEubGFuZy5JbnRlZ2VyIl19");
				MasterBCompComp masterBCompComp = PlayerManagerTest.this.manager.getBySignature(signatureBean);				
				
				signatureBean = PlayerManagerTest.this.manager.deserializeSignature("eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAubWFzdGVyQkNvbXBDb21wLmRldGFpbEFFbnRDb2wiLCJyYXdLZXlWYWx1ZXMiOlsiMSIsIjEiXSwicmF3S2V5VHlwZU5hbWVzIjpbImphdmEubGFuZy5JbnRlZ2VyIiwiamF2YS5sYW5nLkludGVnZXIiXX0");
				Collection<DetailAEnt> compCompDetailAEntCol = PlayerManagerTest.this.manager.getBySignature(signatureBean);
				
				
				Assert.assertThat("masterBEnt.getMasterBComp(), sameInstance(masterBComp)", masterBEnt.getMasterBComp(), sameInstance(masterBComp));
				Assert.assertThat("masterBEnt.getMasterBComp().getMasterBCompComp(), sameInstance(masterBCompComp)", masterBEnt.getMasterBComp().getMasterBCompComp(), sameInstance(masterBCompComp));
				
				Assert.assertThat("masterBComp.getMasterBCompComp(), sameInstance(masterBCompComp)", masterBComp.getMasterBCompComp(), sameInstance(masterBCompComp));
				Assert.assertThat("detailAEntCol, not(sameInstance(compDetailAEntCol))", detailAEntCol, not(sameInstance(compDetailAEntCol)));
				Assert.assertThat("detailAEntCol, not(sameInstance(compCompDetailAEntCol))", detailAEntCol, not(sameInstance(compCompDetailAEntCol)));
				Assert.assertThat("compDetailAEntCol, not(sameInstance(compCompDetailAEntCol))", compDetailAEntCol, not(sameInstance(compCompDetailAEntCol)));
				
				sqlLogInspetor.disable();
				
				return null;
			}
			
		});
		PlayerManagerTest.this.manager.stopJsonWriteIntersept();
	}
//eyJjbGF6ek5hbWUiOiJici5nb3Yuc2VycHJvLndlYmFuYWxpc2UuanNIYlN1cGVyU3luYy5lbnRpdGllcy5NYXN0ZXJCRW50IiwiaXNDb21wIjp0cnVlLCJwcm9wZXJ0eU5hbWUiOiJtYXN0ZXJCQ29tcCIsInJhd0tleVZhbHVlcyI6WyIxIiwiMSJdLCJyYXdLZXlUeXBlTmFtZXMiOlsiamF2YS5sYW5nLkludGVnZXIiLCJqYXZhLmxhbmcuSW50ZWdlciJdfQ
}
