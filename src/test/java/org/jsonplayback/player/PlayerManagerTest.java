package org.jsonplayback.player;

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
import java.util.Set;
import java.util.TimeZone;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.jsonplayback.player.hibernate.CriterionCompat;
import org.jsonplayback.player.hibernate.HibernateJpaCompat;
import org.jsonplayback.player.hibernate.OrderCompat;
import org.jsonplayback.player.hibernate.RestrictionsCompat;
import org.jsonplayback.player.hibernate.entities.DetailAComp;
import org.jsonplayback.player.hibernate.entities.DetailACompComp;
import org.jsonplayback.player.hibernate.entities.DetailACompId;
import org.jsonplayback.player.hibernate.entities.DetailAEnt;
import org.jsonplayback.player.hibernate.entities.DetailARefererEnt;
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import config.org.jsonplayback.player.PlayerManagerTestConfig;

@ContextConfiguration(classes=PlayerManagerTestConfig.class)
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
					DetailAEnt[] detailARefererDetailAEntArr = new DetailAEnt[6];
					List<Object> objectsToSaveList = new ArrayList<>();
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
						if (i < detailARefererDetailAEntArr.length) {
							detailARefererDetailAEntArr[i] = detailAEnt;
						}
					}
					for (int i = 0; i < 10; i++) {
						DetailARefererEnt detailARefererEnt = new DetailARefererEnt();
						detailARefererEnt.setId(i);
						detailARefererEnt.setVcharA(MessageFormat.format("DetailARefererEnt_REG{0,number,00}_REG01_VcharA", i));
						int detailARefererDetailAEntIndex = i % detailARefererDetailAEntArr.length;
						detailARefererEnt.setDetailA(detailARefererDetailAEntArr[detailARefererDetailAEntIndex]);
						objectsToSaveList.add(detailARefererEnt);
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
					List<Object> objectsToSaveList = new ArrayList<>();
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
    	} else if (this.applicationContext.containsBean("&localSessionFactoryBeanCustomizedPersistence")) {
    		return this.applicationContext.getBean("&localSessionFactoryBeanCustomizedPersistence");    		
    	}  else {
    		return null;
    	}
    }
    
    
    @Autowired
    PlatformTransactionManager transactionManager;
    
	@Test
	public void masterATest() throws Exception {	
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
		
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
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
				
				IPlayerSnapshot<MasterAEnt> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAEnt);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+ "."+methodName+"_result_expected.json")
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
	public void masterAWithCustomMetadataTest() throws Exception {		
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
		
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
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
											.configSerialiseBySignatureAllRelationship(true)
											.configMetadataInstantiator(
													(manager) -> {
														return (
																new PlayerMetadatas(PlayerManagerTest.this.manager) {
																	@JsonProperty("$customized-metadata-id-name$")
																	@JsonInclude(Include.NON_NULL)
																	private Long id;
																	@Override
																	public Long getId() {
																		return super.getId();
																	}
																	@Override
																	public void setId(Long id) {
																		super.setId(id);
																	}
																}
														);
													}
											)
						);
				
				IPlayerSnapshot<MasterAEnt> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAEnt);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
					
		try {
			Session ss = this.sessionFactory.openSession();
			String generatedFileResult = "target/" + PlayerManagerTest.class.getSimpleName()
					+ "."+methodName+"_result_generated.json";

			TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
			transactionTemplate.execute(new TransactionCallback<Object>() {

				@Override
				public Object doInTransaction(TransactionStatus arg0) {
					PlayerManagerTest.this.manager.startJsonWriteIntersept();
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

					IPlayerSnapshot<MasterAEnt> playerSnapshot = PlayerManagerTest.this.manager
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
					new InputStreamReader(classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+ "."+methodName+"_result_expected.json")));
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
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		try {
			this.setUpCustom(1000);
			Session ss = this.sessionFactory.openSession();
			String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
			ObjPersistenceSupport objPersistenceSupport = ((IPlayerManagerImplementor)this.manager).getObjPersistenceSupport();
			transactionTemplate.execute(new TransactionCallback<Object>() {
				
				@Override
				public Object doInTransaction(TransactionStatus arg0) {
					PlayerManagerTest.this.manager.startJsonWriteIntersept();
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
					
					IPlayerSnapshot<List<MasterAEnt>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAEntList);
					
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
									classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
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
				
				IPlayerSnapshot<List<MasterAEnt>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAEntList);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
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
				
				IPlayerSnapshot<List<MasterBEnt>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterBEntList);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
				
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		ObjPersistenceSupport objPersistenceSupport = ((IPlayerManagerImplementor)this.manager).getObjPersistenceSupport();
		
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
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
				
				IPlayerSnapshot<List<DetailACompId>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailACompIdList);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
				
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		ObjPersistenceSupport objPersistenceSupport = ((IPlayerManagerImplementor)this.manager).getObjPersistenceSupport();
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
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
				
				IPlayerSnapshot<List<DetailACompId>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailACompIdList);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
				
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
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
				
				IPlayerSnapshot<List<DetailACompComp>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailACompIdList);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
				
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		ObjPersistenceSupport objPersistenceSupport = ((IPlayerManagerImplementor)this.manager).getObjPersistenceSupport();
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
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
				
				IPlayerSnapshot<List<DetailACompComp>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailACompIdList);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
				
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		ObjPersistenceSupport objPersistenceSupport = ((IPlayerManagerImplementor)this.manager).getObjPersistenceSupport();
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
				List<Map<String, Map<String, MasterBEnt>>> masterBEntBizarreList = new ArrayList<>();
				
				for (MasterBEnt masterB : masterBEntList) {
					SignatureBean signBean = ((IPlayerManagerImplementor) PlayerManagerTest.this.manager).generateSignature(masterB);
					String signStr = PlayerManagerTest.this.manager.serializeSignature(signBean);
					Map<String,  Map<String, MasterBEnt>> mapItem = new LinkedHashMap<>();
					IPlayerSnapshot<MasterBEnt> masterBPS = PlayerManagerTest.this.manager.createPlayerSnapshot(masterB);
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
				
				IPlayerSnapshot<List<Map<String, Map<String, MasterBEnt>>>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterBEntBizarreList);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		try {
			Session ss = this.sessionFactory.openSession();
			String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			
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
					
					IPlayerSnapshot<MasterAEnt> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAEnt);
					
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
						classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			
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
				
				IPlayerSnapshot<MasterAEnt> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAEnt);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			
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
				
				IPlayerSnapshot<List<MasterAWrapper>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAWrapperList);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			
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
				
				IPlayerSnapshot<MasterAEnt> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAEnt);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
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
				
				SignatureBean signatureBean = PlayerManagerTest.this.manager.deserializeSignature(PlayerManagerTest.this.getMasterAEntDetailAColKey0Sign());
				Collection<DetailAEnt> detailAEntCol = PlayerManagerTest.this.manager.getBySignature(signatureBean);
				IPlayerSnapshot<Collection<DetailAEnt>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailAEntCol);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
	public void detailAAllBySignTest() throws HibernateException, SQLException, JsonGenerationException, JsonMappingException, IOException {
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				List<DetailAEnt> detailAEntsList =
						PlayerManagerTest.this.hibernateJpaCompat.createCriteria(ss, DetailAEnt.class)
							.addOrder(OrderCompat.asc("id")).list();
				
				PlayerManagerTest.this
					.manager
					.overwriteConfigurationTemporarily(
						PlayerManagerTest
							.this
								.manager
									.getConfig()
										.clone()
										.configSerialiseBySignatureAllRelationship(true));
				
				Map<String, IPlayerSnapshot<DetailAEnt>> allMasteraBySignMap = new LinkedHashMap();
				for (DetailAEnt detailAEnt : detailAEntsList) {
					SignatureBean sign = ((IPlayerManagerImplementor) PlayerManagerTest.this.manager).generateSignature(detailAEnt);
					String signStr = ((IPlayerManagerImplementor) PlayerManagerTest.this.manager).serializeSignature(sign);
					IPlayerSnapshot<DetailAEnt> masteraPlayerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailAEnt);
					allMasteraBySignMap.put(signStr, masteraPlayerSnapshot);
				}
				
				FileOutputStream fos;
				
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, allMasteraBySignMap);
					
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
	public void detailAAllTest() throws HibernateException, SQLException, JsonGenerationException, JsonMappingException, IOException {	
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				List<DetailAEnt> detailAEntList =
						PlayerManagerTest.this.hibernateJpaCompat.createCriteria(ss, DetailAEnt.class)
							.addOrder(OrderCompat.asc("id")).list();
				
				PlayerManagerTest.this
					.manager
					.overwriteConfigurationTemporarily(
						PlayerManagerTest
							.this
								.manager
									.getConfig()
										.clone()
										.configSerialiseBySignatureAllRelationship(true));
				
				IPlayerSnapshot<List<DetailAEnt>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailAEntList);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
	public void detailAAllLazyLoadedTest() throws HibernateException, SQLException, JsonGenerationException, JsonMappingException, IOException {	
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				MasterAEnt masterAEnt = (MasterAEnt) ss.get(MasterAEnt.class, 0);
				DetailACompId id = new DetailACompId();
				id.setSubId(0);
				id.setMasterA(masterAEnt);
				@SuppressWarnings("unchecked")
				DetailAEnt detailAEnt = (DetailAEnt) ss.get(DetailAEnt.class, id);
//						PlayerManagerTest.this.hibernateJpaCompat.createCriteria(ss, DetailAEnt.class)
//							.add((CriterionCompat<DetailAEnt, DetailACompId>) RestrictionsCompat.eq("id", id))
//							.uniqueResult();
				
				detailAEnt.getDetailAComp().getMasterB().getVcharA();
				for (DetailAEnt detailItem : detailAEnt.getCompId().getMasterA().getDetailAEntCol()) {
					detailItem.getDetailAComp().getMasterB().getVcharA();
					detailItem.getDetailAComp().getMasterB().getDetailAEntCol().size();
					detailItem.getDetailAComp().getMasterB().getMasterBComp().getDetailAEntCol().size();
					detailItem.getDetailAComp().getMasterB().getMasterBComp().getMasterBCompComp().getDetailAEntCol().size();
					detailItem.getDetailAComp().getDetailACompComp().getMasterB().getVcharA();
					detailItem.getDetailAComp().getDetailACompComp().getMasterB().getDetailAEntCol().size();
					detailItem.getCompId().getMasterA().getVcharA();
					detailItem.getCompId().getMasterA().getVcharA();
				}				
				
				PlayerManagerTest.this
					.manager
					.overwriteConfigurationTemporarily(
						PlayerManagerTest
							.this
								.manager
									.getConfig()
										.clone()
										.configSerialiseBySignatureAllRelationship(false));
				
				IPlayerSnapshot<DetailAEnt> detailAEntPlayerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailAEnt);
				
				FileOutputStream fos;
				
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, detailAEntPlayerSnapshot);
					
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
	public void detailAEntColAllBySignTest() throws HibernateException, SQLException, JsonGenerationException, JsonMappingException, IOException {	
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				List<MasterAEnt> masterAEntList =
						PlayerManagerTest.this.hibernateJpaCompat.createCriteria(ss, MasterAEnt.class)
							.addOrder(OrderCompat.asc("id")).list();
				
				PlayerManagerTest.this
					.manager
					.overwriteConfigurationTemporarily(
						PlayerManagerTest
							.this
								.manager
									.getConfig()
										.clone()
										.configSerialiseBySignatureAllRelationship(true));
				
				Map<String, IPlayerSnapshot<Set<DetailAEnt>>> allMasteraBySignMap = new LinkedHashMap();
				for (MasterAEnt masterAEnt : masterAEntList) {
					SignatureBean sign = ((IPlayerManagerImplementor) PlayerManagerTest.this.manager).generateLazySignatureForCollRelashionship(MasterAEnt.class, "detailAEntCol", masterAEnt, masterAEnt.getDetailAEntCol());
					String signStr = ((IPlayerManagerImplementor) PlayerManagerTest.this.manager).serializeSignature(sign);
					IPlayerSnapshot<Set<DetailAEnt>> masteraPlayerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAEnt.getDetailAEntCol());
					allMasteraBySignMap.put(signStr, masteraPlayerSnapshot);
				}
				
				FileOutputStream fos;
				
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, allMasteraBySignMap);
					
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
	public void detailARefererAllTest() throws HibernateException, SQLException, JsonGenerationException, JsonMappingException, IOException {	
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				List<DetailARefererEnt> detailARefererEntList =
						PlayerManagerTest.this.hibernateJpaCompat.createCriteria(ss, DetailARefererEnt.class)
							.addOrder(OrderCompat.asc("id")).list();
				
				PlayerManagerTest.this
					.manager
					.overwriteConfigurationTemporarily(
						PlayerManagerTest
							.this
								.manager
									.getConfig()
										.clone()
										.configSerialiseBySignatureAllRelationship(true));
				
				IPlayerSnapshot<List<DetailARefererEnt>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailARefererEntList);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
	public void detailARefererAllBySignTest() throws HibernateException, SQLException, JsonGenerationException, JsonMappingException, IOException {
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				List<DetailARefererEnt> detailARefererEntList =
						PlayerManagerTest.this.hibernateJpaCompat.createCriteria(ss, DetailARefererEnt.class)
							.addOrder(OrderCompat.asc("id")).list();
				
				PlayerManagerTest.this
					.manager
					.overwriteConfigurationTemporarily(
						PlayerManagerTest
							.this
								.manager
									.getConfig()
										.clone()
										.configSerialiseBySignatureAllRelationship(true));
				Map<String, IPlayerSnapshot<DetailARefererEnt>> allDetailARefererBySignMap = new LinkedHashMap<>();
				
				for (DetailARefererEnt detailARefererEnt : detailARefererEntList) {
					SignatureBean sign = ((IPlayerManagerImplementor) PlayerManagerTest.this.manager).generateSignature(detailARefererEnt);
					String signStr = ((IPlayerManagerImplementor) PlayerManagerTest.this.manager).serializeSignature(sign);
					IPlayerSnapshot<DetailARefererEnt> detailARefererPlayerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailARefererEnt);
					allDetailARefererBySignMap.put(signStr, detailARefererPlayerSnapshot);
				}
				
				FileOutputStream fos;
				
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, allDetailARefererBySignMap);
					
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
	public void masterAAllTest() throws HibernateException, SQLException, JsonGenerationException, JsonMappingException, IOException {	
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				List<MasterAEnt> masterAEntList =
						PlayerManagerTest.this.hibernateJpaCompat.createCriteria(ss, MasterAEnt.class)
							.addOrder(OrderCompat.asc("id")).list();
				
				PlayerManagerTest.this
					.manager
					.overwriteConfigurationTemporarily(
						PlayerManagerTest
							.this
								.manager
									.getConfig()
										.clone()
										.configSerialiseBySignatureAllRelationship(true));
				
				List<IPlayerSnapshot<MasterAEnt>> allMasteraList = new ArrayList<>();
				for (MasterAEnt masterAEnt : masterAEntList) {
					IPlayerSnapshot<MasterAEnt> masteraPlayerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAEnt);
					allMasteraList.add(masteraPlayerSnapshot);
				}
				
				FileOutputStream fos;
				
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, allMasteraList);
					
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
	public void masterAAllBySignTest() throws HibernateException, SQLException, JsonGenerationException, JsonMappingException, IOException {	
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
				
				List<MasterAEnt> masterAEntList =
						PlayerManagerTest.this.hibernateJpaCompat.createCriteria(ss, MasterAEnt.class)
							.addOrder(OrderCompat.asc("id")).list();
				
				PlayerManagerTest.this
					.manager
					.overwriteConfigurationTemporarily(
						PlayerManagerTest
							.this
								.manager
									.getConfig()
										.clone()
										.configSerialiseBySignatureAllRelationship(true));
				
				Map<String, IPlayerSnapshot<MasterAEnt>> allMasteraBySignMap = new LinkedHashMap();
				for (MasterAEnt masterAEnt : masterAEntList) {
					SignatureBean sign = ((IPlayerManagerImplementor) PlayerManagerTest.this.manager).generateSignature(masterAEnt);
					String signStr = ((IPlayerManagerImplementor) PlayerManagerTest.this.manager).serializeSignature(sign);
					IPlayerSnapshot<MasterAEnt> masteraPlayerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterAEnt);
					allMasteraBySignMap.put(signStr, masteraPlayerSnapshot);
				}
				
				FileOutputStream fos;
				
				try {
					fos = new FileOutputStream(generatedFileResult);
					PlayerManagerTest
						.this
							.manager
								.getConfig()
									.getObjectMapper()
										.writerWithDefaultPrettyPrinter()
											.writeValue(fos, allMasteraBySignMap);
					
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			
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
				
				SignatureBean signatureBean = PlayerManagerTest.this.manager.deserializeSignature(PlayerManagerTest.this.getMasterAEntDetailAColKey0Sign());
				Collection<DetailAEnt> detailAEntCol = PlayerManagerTest.this.manager.getBySignature(signatureBean);
				ArrayList<DetailAEnt> detailAEntCuttedCol = new ArrayList<>();
				detailAEntCuttedCol.add(new ArrayList<>(detailAEntCol).get(0));
				detailAEntCuttedCol.add(new ArrayList<>(detailAEntCol).get(1));
				IPlayerSnapshot<Collection<DetailAEnt>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailAEntCuttedCol);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
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
				
				SignatureBean signatureBean = PlayerManagerTest.this.manager.deserializeSignature(PlayerManagerTest.this.getMasterAEntDetailAColKey0Sign());
				Collection<DetailAEnt> detailAEntCol = PlayerManagerTest.this.manager.getBySignature(signatureBean);
				ArrayList<DetailAEnt> detailAEntCuttedCol = new ArrayList<>();
				detailAEntCuttedCol.add(new ArrayList<>(detailAEntCol).get(1));
				detailAEntCuttedCol.add(new ArrayList<>(detailAEntCol).get(2));
				IPlayerSnapshot<Collection<DetailAEnt>> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(detailAEntCuttedCol);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
					
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
		
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
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
				
				IPlayerSnapshot<MasterBEnt> playerSnapshot = PlayerManagerTest.this.manager.createPlayerSnapshot(masterBEnt);
				
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
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
	
	/**
	 * "org.hibernate.TransientObjectException: object references an unsaved transient instance - save the transient instance before flushing..." on 
	 * PlayerManagerTest.detailAKey0c0GetBySignTest() using Jpa mode.
	 * @throws Exception
	 */
	@Test
	public void detailAKey0c0GetBySignTest() throws Exception {	
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
								
				SignatureBean signatureBean = PlayerManagerTest.this.manager.deserializeSignature(PlayerManagerTest.this.getDetailAEntKey0c0Sign());
				DetailAEnt detailAEnt = PlayerManagerTest.this.manager.getBySignature(signatureBean);
				
				sqlLogInspetor.disable();
				
				return null;
			}
			
		});
		PlayerManagerTest.this.manager.stopJsonWriteIntersept();
	}
	
	@Test
	public void masterBInnerCompsGetBySigTest() throws Exception {	
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Map<String, Charset> availableCharsetsMap = Charset.availableCharsets();
//		for (String keyCS : availableCharsetsMap.keySet()) {
//			System.out.println(">>>>>>>: "+keyCS+"= "+ availableCharsetsMap.get(keyCS).displayName());
//		}
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
			
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				PlayerManagerTest.this.manager.startJsonWriteIntersept();
				//SchemaExport
				
				//Configuration hbConfiguration = PlayerManagerTest.this.localSessionFactoryBean.getConfiguration();
				
				SqlLogInspetor sqlLogInspetor = new SqlLogInspetor();
				sqlLogInspetor.enable();
								
				SignatureBean signatureBean = PlayerManagerTest.this.manager.deserializeSignature(PlayerManagerTest.this.getMasterBEntKey1c1Sign());
				MasterBEnt masterBEnt = PlayerManagerTest.this.manager.getBySignature(signatureBean);
				
				signatureBean = PlayerManagerTest.this.manager.deserializeSignature(PlayerManagerTest.this.getMasterAEntDetailAColKey0Sign());
				Collection<DetailAEnt> detailAEntCol = PlayerManagerTest.this.manager.getBySignature(signatureBean);
				
				signatureBean = PlayerManagerTest.this.manager.deserializeSignature(PlayerManagerTest.this.getMasterBEntMasterBCompKey1c1Sign());
				MasterBComp masterBComp = PlayerManagerTest.this.manager.getBySignature(signatureBean);

				signatureBean = PlayerManagerTest.this.manager.deserializeSignature(PlayerManagerTest.this.getMasterBEntMasterBCompDetailAEntColKey1c1Sign());
				Collection<DetailAEnt> compDetailAEntCol = PlayerManagerTest.this.manager.getBySignature(signatureBean);

				signatureBean = PlayerManagerTest.this.manager.deserializeSignature(PlayerManagerTest.this.getMasterBEntMasterBCompMasterBCompCompKey1c1Sign());
				MasterBCompComp masterBCompComp = PlayerManagerTest.this.manager.getBySignature(signatureBean);				
				
				signatureBean = PlayerManagerTest.this.manager.deserializeSignature(PlayerManagerTest.this.getMasterBEntMasterBCompMasterBCompCompDetailAEntColKey1c1Sign());
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
	
	@Test
	public void nonStartedmanagerTest() throws Exception {	
		String methodName = new Object() {}
			.getClass()
			.getEnclosingMethod()
			.getName();
			
		Session ss = this.sessionFactory.openSession();
		String generatedFileResult = "target/"+PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_generated.json";
		FileOutputStream fos;
		
		try {
			fos = new FileOutputStream(generatedFileResult);
			ObjectMapper objectMapper = PlayerManagerTest
				.this
					.manager
						.getConfig()
							.getObjectMapper();
			objectMapper
				.writerWithDefaultPrettyPrinter()
					.writeValue(
							fos, 
							new Object() {
								private String fooField = "barvalue";
								@SuppressWarnings("unused")
								public String getFooField() {
									return fooField;
								}
								@SuppressWarnings("unused")
								public void setFooField(String fooField) {
									this.fooField = fooField;
								}
							}
					);
		} catch (Exception e) {
			throw new RuntimeException("Unexpected", e);
		}
		
		ClassLoader classLoader = getClass().getClassLoader();
		BufferedReader brExpected = 
			new BufferedReader(
				new InputStreamReader(
					classLoader.getResourceAsStream(PlayerManagerTest.class.getPackage().getName().replaceAll("\\.", "/")+"/"+this.getResourceFolder()+"/"+ PlayerManagerTest.class.getSimpleName()+"."+methodName+"_result_expected.json")
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
//eyJjbGF6ek5hbWUiOiJici5nb3Yuc2VycHJvLndlYmFuYWxpc2UuanNIYlN1cGVyU3luYy5lbnRpdGllcy5NYXN0ZXJCRW50IiwiaXNDb21wIjp0cnVlLCJwcm9wZXJ0eU5hbWUiOiJtYXN0ZXJCQ29tcCIsInJhd0tleVZhbHVlcyI6WyIxIiwiMSJdLCJyYXdLZXlUeXBlTmFtZXMiOlsiamF2YS5sYW5nLkludGVnZXIiLCJqYXZhLmxhbmcuSW50ZWdlciJdfQ
	
	
	private String getMasterAEntDetailAColKey0Sign() {
		if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB3) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQUVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoiZGV0YWlsQUVudENvbCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJyYXdLZXlWYWx1ZXNcIjpbXCIwXCJdLFwicmF3S2V5VHlwZU5hbWVzXCI6W1wiamF2YS5sYW5nLkludGVnZXJcIl19In0";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB4) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQUVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoiZGV0YWlsQUVudENvbCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJyYXdLZXlWYWx1ZXNcIjpbXCIwXCJdLFwicmF3S2V5VHlwZU5hbWVzXCI6W1wiamF2YS5sYW5nLkludGVnZXJcIl19In0";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB5) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQUVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoiZGV0YWlsQUVudENvbCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJyYXdLZXlWYWx1ZXNcIjpbXCIwXCJdLFwicmF3S2V5VHlwZU5hbWVzXCI6W1wiamF2YS5sYW5nLkludGVnZXJcIl19In0";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.JPA) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQUVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoiZGV0YWlsQUVudENvbCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJpZFwiOjB9In0";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.CUSTOMIZED_PERSISTENCE) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQUVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoiZGV0YWlsQUVudENvbCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJpZFwiOjB9In0" + ObjPersistenceMode.CUSTOMIZED_PERSISTENCE;
		} else {
			throw new RuntimeException("This should not happen");
		}
	}

	private String getMasterBEntKey1c1Sign() {
		if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB3) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJyYXdLZXlWYWx1ZXNcIjpbXCIxXCIsXCIxXCJdLFwicmF3S2V5VHlwZU5hbWVzXCI6W1wiamF2YS5sYW5nLkludGVnZXJcIixcImphdmEubGFuZy5JbnRlZ2VyXCJdfSJ9";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB4) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJyYXdLZXlWYWx1ZXNcIjpbXCIxXCIsXCIxXCJdLFwicmF3S2V5VHlwZU5hbWVzXCI6W1wiamF2YS5sYW5nLkludGVnZXJcIixcImphdmEubGFuZy5JbnRlZ2VyXCJdfSJ9";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB5) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJyYXdLZXlWYWx1ZXNcIjpbXCIxXCIsXCIxXCJdLFwicmF3S2V5VHlwZU5hbWVzXCI6W1wiamF2YS5sYW5nLkludGVnZXJcIixcImphdmEubGFuZy5JbnRlZ2VyXCJdfSJ9";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.JPA) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJjb21wSWRcIjp7XCJpZEFcIjoxLFwiaWRCXCI6MX19In0";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.CUSTOMIZED_PERSISTENCE) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJjb21wSWRcIjp7XCJpZEFcIjoxLFwiaWRCXCI6MX19In0"+ ObjPersistenceMode.CUSTOMIZED_PERSISTENCE;
		} else {
			throw new RuntimeException("This should not happen");
		}
	}

	private String getMasterBEntMasterBCompKey1c1Sign() {
		if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB3) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29tcCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAiLCJzdHJpbmdpZmllZE9iamVjdElkIjoie1wicmF3S2V5VmFsdWVzXCI6W1wiMVwiLFwiMVwiXSxcInJhd0tleVR5cGVOYW1lc1wiOltcImphdmEubGFuZy5JbnRlZ2VyXCIsXCJqYXZhLmxhbmcuSW50ZWdlclwiXX0ifQ";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB4) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29tcCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAiLCJzdHJpbmdpZmllZE9iamVjdElkIjoie1wicmF3S2V5VmFsdWVzXCI6W1wiMVwiLFwiMVwiXSxcInJhd0tleVR5cGVOYW1lc1wiOltcImphdmEubGFuZy5JbnRlZ2VyXCIsXCJqYXZhLmxhbmcuSW50ZWdlclwiXX0ifQ";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB5) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29tcCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAiLCJzdHJpbmdpZmllZE9iamVjdElkIjoie1wicmF3S2V5VmFsdWVzXCI6W1wiMVwiLFwiMVwiXSxcInJhd0tleVR5cGVOYW1lc1wiOltcImphdmEubGFuZy5JbnRlZ2VyXCIsXCJqYXZhLmxhbmcuSW50ZWdlclwiXX0ifQ";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.JPA) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29tcCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAiLCJzdHJpbmdpZmllZE9iamVjdElkIjoie1wiY29tcElkXCI6e1wiaWRBXCI6MSxcImlkQlwiOjF9fSJ9";	
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.CUSTOMIZED_PERSISTENCE) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29tcCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAiLCJzdHJpbmdpZmllZE9iamVjdElkIjoie1wiY29tcElkXCI6e1wiaWRBXCI6MSxcImlkQlwiOjF9fSJ9" + ObjPersistenceMode.CUSTOMIZED_PERSISTENCE;
		} else {
			throw new RuntimeException("This should not happen");
		}
	}

	private String getMasterBEntMasterBCompDetailAEntColKey1c1Sign() {
		if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB3) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAuZGV0YWlsQUVudENvbCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJyYXdLZXlWYWx1ZXNcIjpbXCIxXCIsXCIxXCJdLFwicmF3S2V5VHlwZU5hbWVzXCI6W1wiamF2YS5sYW5nLkludGVnZXJcIixcImphdmEubGFuZy5JbnRlZ2VyXCJdfSJ9";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB4) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAuZGV0YWlsQUVudENvbCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJyYXdLZXlWYWx1ZXNcIjpbXCIxXCIsXCIxXCJdLFwicmF3S2V5VHlwZU5hbWVzXCI6W1wiamF2YS5sYW5nLkludGVnZXJcIixcImphdmEubGFuZy5JbnRlZ2VyXCJdfSJ9";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB5) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAuZGV0YWlsQUVudENvbCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJyYXdLZXlWYWx1ZXNcIjpbXCIxXCIsXCIxXCJdLFwicmF3S2V5VHlwZU5hbWVzXCI6W1wiamF2YS5sYW5nLkludGVnZXJcIixcImphdmEubGFuZy5JbnRlZ2VyXCJdfSJ9";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.JPA) {			
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAuZGV0YWlsQUVudENvbCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJjb21wSWRcIjp7XCJpZEFcIjoxLFwiaWRCXCI6MX19In0";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.CUSTOMIZED_PERSISTENCE) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAuZGV0YWlsQUVudENvbCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJjb21wSWRcIjp7XCJpZEFcIjoxLFwiaWRCXCI6MX19In0" + ObjPersistenceMode.CUSTOMIZED_PERSISTENCE;
		} else {
			throw new RuntimeException("This should not happen");
		}
	}

	private String getMasterBEntMasterBCompMasterBCompCompKey1c1Sign() {
		if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB3) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29tcCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAubWFzdGVyQkNvbXBDb21wIiwic3RyaW5naWZpZWRPYmplY3RJZCI6IntcInJhd0tleVZhbHVlc1wiOltcIjFcIixcIjFcIl0sXCJyYXdLZXlUeXBlTmFtZXNcIjpbXCJqYXZhLmxhbmcuSW50ZWdlclwiLFwiamF2YS5sYW5nLkludGVnZXJcIl19In0";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB4) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29tcCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAubWFzdGVyQkNvbXBDb21wIiwic3RyaW5naWZpZWRPYmplY3RJZCI6IntcInJhd0tleVZhbHVlc1wiOltcIjFcIixcIjFcIl0sXCJyYXdLZXlUeXBlTmFtZXNcIjpbXCJqYXZhLmxhbmcuSW50ZWdlclwiLFwiamF2YS5sYW5nLkludGVnZXJcIl19In0";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB5) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29tcCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAubWFzdGVyQkNvbXBDb21wIiwic3RyaW5naWZpZWRPYmplY3RJZCI6IntcInJhd0tleVZhbHVlc1wiOltcIjFcIixcIjFcIl0sXCJyYXdLZXlUeXBlTmFtZXNcIjpbXCJqYXZhLmxhbmcuSW50ZWdlclwiLFwiamF2YS5sYW5nLkludGVnZXJcIl19In0";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.JPA) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29tcCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAubWFzdGVyQkNvbXBDb21wIiwic3RyaW5naWZpZWRPYmplY3RJZCI6IntcImNvbXBJZFwiOntcImlkQVwiOjEsXCJpZEJcIjoxfX0ifQ";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.CUSTOMIZED_PERSISTENCE) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29tcCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAubWFzdGVyQkNvbXBDb21wIiwic3RyaW5naWZpZWRPYmplY3RJZCI6IntcImNvbXBJZFwiOntcImlkQVwiOjEsXCJpZEJcIjoxfX0ifQ"+ ObjPersistenceMode.CUSTOMIZED_PERSISTENCE;
		} else {
			throw new RuntimeException("This should not happen");
		}	
	}

	private String getMasterBEntMasterBCompMasterBCompCompDetailAEntColKey1c1Sign() {
		if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB3) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAubWFzdGVyQkNvbXBDb21wLmRldGFpbEFFbnRDb2wiLCJzdHJpbmdpZmllZE9iamVjdElkIjoie1wicmF3S2V5VmFsdWVzXCI6W1wiMVwiLFwiMVwiXSxcInJhd0tleVR5cGVOYW1lc1wiOltcImphdmEubGFuZy5JbnRlZ2VyXCIsXCJqYXZhLmxhbmcuSW50ZWdlclwiXX0ifQ";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB4) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAubWFzdGVyQkNvbXBDb21wLmRldGFpbEFFbnRDb2wiLCJzdHJpbmdpZmllZE9iamVjdElkIjoie1wicmF3S2V5VmFsdWVzXCI6W1wiMVwiLFwiMVwiXSxcInJhd0tleVR5cGVOYW1lc1wiOltcImphdmEubGFuZy5JbnRlZ2VyXCIsXCJqYXZhLmxhbmcuSW50ZWdlclwiXX0ifQ";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB5) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAubWFzdGVyQkNvbXBDb21wLmRldGFpbEFFbnRDb2wiLCJzdHJpbmdpZmllZE9iamVjdElkIjoie1wicmF3S2V5VmFsdWVzXCI6W1wiMVwiLFwiMVwiXSxcInJhd0tleVR5cGVOYW1lc1wiOltcImphdmEubGFuZy5JbnRlZ2VyXCIsXCJqYXZhLmxhbmcuSW50ZWdlclwiXX0ifQ";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.JPA) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAubWFzdGVyQkNvbXBDb21wLmRldGFpbEFFbnRDb2wiLCJzdHJpbmdpZmllZE9iamVjdElkIjoie1wiY29tcElkXCI6e1wiaWRBXCI6MSxcImlkQlwiOjF9fSJ9";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.CUSTOMIZED_PERSISTENCE) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuTWFzdGVyQkVudCIsImlzQ29sbCI6dHJ1ZSwicHJvcGVydHlOYW1lIjoibWFzdGVyQkNvbXAubWFzdGVyQkNvbXBDb21wLmRldGFpbEFFbnRDb2wiLCJzdHJpbmdpZmllZE9iamVjdElkIjoie1wiY29tcElkXCI6e1wiaWRBXCI6MSxcImlkQlwiOjF9fSJ9" + ObjPersistenceMode.CUSTOMIZED_PERSISTENCE;
		} else {
			throw new RuntimeException("This should not happen");
		}
	}
	
	private String getDetailAEntKey0c0Sign() {
		if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB3) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuRGV0YWlsQUVudCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJyYXdLZXlWYWx1ZXNcIjpbXCIwXCIsXCIwXCJdLFwicmF3S2V5VHlwZU5hbWVzXCI6W1wiamF2YS5sYW5nLkludGVnZXJcIixcImphdmEubGFuZy5JbnRlZ2VyXCJdfSJ9";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB4) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuRGV0YWlsQUVudCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJyYXdLZXlWYWx1ZXNcIjpbXCIwXCIsXCIwXCJdLFwicmF3S2V5VHlwZU5hbWVzXCI6W1wiamF2YS5sYW5nLkludGVnZXJcIixcImphdmEubGFuZy5JbnRlZ2VyXCJdfSJ9";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB5) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuRGV0YWlsQUVudCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJyYXdLZXlWYWx1ZXNcIjpbXCIwXCIsXCIwXCJdLFwicmF3S2V5VHlwZU5hbWVzXCI6W1wiamF2YS5sYW5nLkludGVnZXJcIixcImphdmEubGFuZy5JbnRlZ2VyXCJdfSJ9";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.JPA) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuRGV0YWlsQUVudCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJjb21wSWRcIjp7XCJtYXN0ZXJBXCI6e1wiaWRcIjowfSxcInN1YklkXCI6MH19In0";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.CUSTOMIZED_PERSISTENCE) {
			return "manager01.eyJjbGF6ek5hbWUiOiJvcmcuanNvbnBsYXliYWNrLnBsYXllci5oaWJlcm5hdGUuZW50aXRpZXMuRGV0YWlsQUVudCIsInN0cmluZ2lmaWVkT2JqZWN0SWQiOiJ7XCJjb21wSWRcIjp7XCJtYXN0ZXJBXCI6e1wiaWRcIjowfSxcInN1YklkXCI6MH19In0" + ObjPersistenceMode.CUSTOMIZED_PERSISTENCE;
		} else {
			throw new RuntimeException("This should not happen");
		}
	}
	
	private String getResourceFolder() {
		if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB3) {
			return "hb3";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB4) {
			return "hb3";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.HB5) {
			return "hb3";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.JPA) {
			return "jpa";
		} else if (this.manager.getConfig().getObjPersistenceMode() == ObjPersistenceMode.CUSTOMIZED_PERSISTENCE) {
			return "customized-persistence";
		} else {
			throw new RuntimeException("This should not happen");
		}
		
	}
}
