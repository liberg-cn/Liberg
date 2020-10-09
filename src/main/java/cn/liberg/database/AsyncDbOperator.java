package cn.liberg.database;


import cn.liberg.core.OperatorException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * 异步批量执行sql的线程
 */
public class AsyncDbOperator {
	private Log logger = LogFactory.getLog(this.getClass());
	
	private static int SLEEP_INTERVAL=3;
	private static int BATCH_OP_NUMBER=300;
	private static int OP_TYPE_SAVE=1;
	private static int OP_TYPE_UPDATE=2;
	private static int OP_TYPE_DELETE=3;
	private static int OP_TYPE_EXECUTE_SQL=4;

	private ArrayList<OpInfo> mOPList=null;
	private Thread mThread=null;
	private boolean mRunning=false;
	private DBHelper dbHelper;
	
	public AsyncDbOperator(DBHelper dbController){
		mOPList=new ArrayList<OpInfo>();
		dbHelper =dbController;
	} 
	
	public void start(){
		if(mThread==null){
			mThread=initThread();
		} 
		if(mRunning==false){
			mRunning=true;
			mThread.start();
		}
		
	}
	
	public void stop(){
		mRunning=false;		
	}	
	
	public void saveObject(Object obj){
		OpInfo info=new OpInfo();
		info.mOpType=OP_TYPE_SAVE;
		info.mObj=obj;
		synchronized(mOPList){
			mOPList.add(info);
		}
	}
	
	public void updateObj(Object obj){
		OpInfo info=new OpInfo();
		info.mOpType=OP_TYPE_UPDATE;
		info.mObj=obj;
		synchronized(mOPList){
			mOPList.add(info);
		}
	}
	
	public void ExecuteSql(String sql) {
		OpInfo info=new OpInfo();
		info.mOpType=OP_TYPE_EXECUTE_SQL;
		info.mObj=sql;
		synchronized(mOPList){
			mOPList.add(info);
		}
	}
	
	private Thread initThread() {
		Thread thread=new Thread(this.getClass().getName()){
			@Override
            public void run() {		
				ArrayList<OpInfo> infos=new ArrayList<OpInfo>();
				String sql="";
				ArrayList<String> sqls=new ArrayList<String>();
				while(mRunning){
					synchronized(mOPList){
						while(mOPList.size()>0 && infos.size()<BATCH_OP_NUMBER){
							infos.add(mOPList.remove(0));
						}
					}
					if(infos.size()<=0){
						try {
							Thread.sleep(SLEEP_INTERVAL);
						} catch (InterruptedException e) {
							logger.error(e.getMessage(), e);
						}
					} else {
						try {
							for(OpInfo info:infos){
								if(info.mOpType==OP_TYPE_SAVE){	
									sql= DBHelper.buildSaveSql(info.dao, info.mObj);
									sqls.add(sql);
								} else if(info.mOpType==OP_TYPE_UPDATE){
									sql= DBHelper.buildUpdateSql(info.dao, info.mObj);
									sqls.add(sql);
								}  else if(info.mOpType==OP_TYPE_EXECUTE_SQL){
									sqls.add((String)info.mObj);
								}
							}
							try{
								dbHelper.executeSqlBatch(sqls);
							} catch(OperatorException e){
								logger.error(e.getMessage(), e);
							}
						} catch (OperatorException e) {
							logger.error(e.getMessage(), e);
						}
					}	
					sqls.clear();
					infos.clear();
				}
				mThread=null;
			}
		};
		return thread;
	}
	
	private class OpInfo{
		public int mOpType;
		public BaseDao dao;
		public Object mObj;
	}
	
	public int getWaitCount() {
		int count;
		synchronized(mOPList){
			count=mOPList.size();
		}
		return count;
	}
}

