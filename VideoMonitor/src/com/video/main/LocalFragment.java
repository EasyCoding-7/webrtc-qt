package com.video.main;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.video.R;
import com.video.data.XmlDevice;
import com.video.local.ImageListViewAdapter;
import com.video.local.LocalFileItem;
import com.video.local.VideoListViewAdapter;
import com.video.utils.DeviceItemAdapter;
import com.video.utils.TextProgressBar;
import com.video.utils.Utils;
import com.video.utils.ViewPagerAdapter;

public class LocalFragment extends Fragment implements OnClickListener, OnPageChangeListener {

	private FragmentActivity mActivity;
	private View mView;
	
	private TextView viewpage_video;
	private TextView viewpage_image;
	private TextView viewpage_terminal;
	
	private ViewPager mViewPager;
	private List<View> pageList;
	private View video_page;
	private View image_page;
	private View terminal_page;
	private TextProgressBar progressBarSD;
	
	private String SD_path = "";
	
	private final int INIT_LOCAL_IMAGE_FINISH = 1;
	private final int NO_LOCAL_IMAGE_FILE = 2;
	private final int INIT_LOCAL_VIDEO_FINISH = 3;
	private final int NO_LOCAL_VIDEO_FILE = 4;
	
	private LocalReceiver localReceiver = null;
	public static final String REFRESH_VIDEO_FILE = "com.video.main.LocalFragment.refresh_video_file";
	
	
	//抓拍图片初始化
	File currentImageFile = null;
	File[] currentImageFileList = null;
	private int imageFileCount = 0;
	private ListView imageListView = null;
	private ImageListViewAdapter imageListAdapter = null;
	private List<LocalFileItem> imageLocalFile = null;
	private RelativeLayout noImageLayout = null;
	
	//本地录像初始化
	File currentVideoFile = null;
	File[] currentVideoFileList = null;
	private int videoFileCount = 0;
	private ListView videoListView = null;
	private VideoListViewAdapter videoListAdapter = null;
	private List<LocalFileItem> videoLocalFile = null;
	private RelativeLayout noVideoLayout = null;
	
	//终端录像初始化
	private XmlDevice xmlData;
	private String thumbnailsPath = null;
	private File thumbnailsFile = null;
	private ArrayList<HashMap<String, String>> deviceList = null;
	private DeviceItemAdapter deviceAdapter = null;
	private ListView terminalListView = null;
	private int listSize = 0;
	private RelativeLayout noDeviceLayout = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.local, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		mActivity = getActivity();
		mView = getView();
		
		initViewPageView(0);
		initView();
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		initData();
	}
	
	/**
	 * 初始化该界面下要滑动的页面
	 */
	private void initViewPageView(int whichPage) {
		LayoutInflater inflater = LayoutInflater.from(mActivity);
		image_page = inflater.inflate(R.layout.local_image, null);
		video_page = inflater.inflate(R.layout.local_video, null);
		terminal_page = inflater.inflate(R.layout.local_terminal, null);
		
		pageList = new ArrayList<View>();
		pageList.add(image_page);
		pageList.add(video_page);
		pageList.add(terminal_page);
		
		mViewPager = (ViewPager)mView.findViewById(R.id.local_viewpager);
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setAdapter(new ViewPagerAdapter(pageList));
		mViewPager.setCurrentItem(whichPage);
	}
	
	private void initView() {
		viewpage_image = (TextView)mView.findViewById(R.id.tv_vp_image);
		viewpage_image.setOnClickListener(this);
		viewpage_video = (TextView)mView.findViewById(R.id.tv_vp_video);
		viewpage_video.setOnClickListener(this);
		viewpage_terminal = (TextView)mView.findViewById(R.id.tv_vp_terminal);
		viewpage_terminal.setOnClickListener(this);
		
		//抓拍图片初始化
		imageListView = (ListView) image_page.findViewById(R.id.local_image_listView);
		noImageLayout = (RelativeLayout) image_page.findViewById(R.id.rl_no_local_file_image);
				
		//本地录像初始化
		videoListView = (ListView) video_page.findViewById(R.id.local_video_listView);
		noVideoLayout = (RelativeLayout) video_page.findViewById(R.id.rl_no_local_file_video);
		
		//终端录像初始化
		terminalListView = (ListView) terminal_page.findViewById(R.id.lv_local_device_list);
		terminalListView.setOnItemClickListener(new OnItemClickListenerImpl());
		noDeviceLayout = (RelativeLayout) terminal_page.findViewById(R.id.rl_no_local_device_list);
		
		//注册广播
		localReceiver = new LocalReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(REFRESH_VIDEO_FILE);
		mActivity.registerReceiver(localReceiver, filter);
		
		xmlData = new XmlDevice(mActivity);
		
		if (Utils.checkSDCard()) {
			//获得SD的信息
			progressBarSD = (TextProgressBar) mView.findViewById(R.id.progressBar_sd);
			progressBarSD.setMax(100);
			double SDTotal = Utils.getTotalSDSize();
			double SDAvailable = Utils.getAvailableSDSize();
			int SDScale = (int) ((SDAvailable/SDTotal)*100);
			DecimalFormat df = new DecimalFormat("0.00");
			progressBarSD.setText("SD卡："+df.format(SDTotal)+"GB    可用："+df.format(SDAvailable)+"GB");
			progressBarSD.setProgress(SDScale);
		}
	}
	
	private void initData() {
		if (Utils.checkSDCard()) {
			SD_path = Environment.getExternalStorageDirectory().getAbsolutePath();
			//抓拍图片初始化
			new LocalImageThread().start();
			
			//本地录像初始化
			new LocalVideoThread().start();
			
			//终端录像初始化
			thumbnailsPath = SD_path + File.separator + "KaerVideo" + File.separator + "thumbnails";
			thumbnailsFile = new File(thumbnailsPath);
			if(!thumbnailsFile.exists()){
				thumbnailsFile.mkdirs();
			}
			
			deviceList = new ArrayList<HashMap<String, String>>();
			deviceList = xmlData.getOnlineList();
			if (deviceList != null) {
				listSize = deviceList.size();
				deviceAdapter = new DeviceItemAdapter(mActivity, thumbnailsFile, deviceList);
				terminalListView.setAdapter(deviceAdapter);
			
				if (listSize == 0) {
					noDeviceLayout.setVisibility(View.VISIBLE);
					deviceList.clear();
					deviceAdapter.notifyDataSetChanged();
				} else {
					noDeviceLayout.setVisibility(View.INVISIBLE);
				}
			} else {
				listSize = 0;
				noDeviceLayout.setVisibility(View.VISIBLE);
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.tv_vp_image:
				viewpage_image.setBackgroundResource(R.drawable.viewpage_selected);
				viewpage_video.setBackgroundResource(R.drawable.viewpage_unselected);
				viewpage_terminal.setBackgroundResource(R.drawable.viewpage_unselected);
				mViewPager.setCurrentItem(0);
				break;
			case R.id.tv_vp_video:
				viewpage_video.setBackgroundResource(R.drawable.viewpage_selected);
				viewpage_image.setBackgroundResource(R.drawable.viewpage_unselected);
				viewpage_terminal.setBackgroundResource(R.drawable.viewpage_unselected);
				mViewPager.setCurrentItem(1);
				break;
			case R.id.tv_vp_terminal:
				viewpage_terminal.setBackgroundResource(R.drawable.viewpage_selected);
				viewpage_image.setBackgroundResource(R.drawable.viewpage_unselected);
				viewpage_video.setBackgroundResource(R.drawable.viewpage_unselected);
				mViewPager.setCurrentItem(2);
				break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		switch (arg0) {
			case 0:
				viewpage_image.setBackgroundResource(R.drawable.viewpage_selected);
				viewpage_video.setBackgroundResource(R.drawable.viewpage_unselected);
				viewpage_terminal.setBackgroundResource(R.drawable.viewpage_unselected);
				mViewPager.setCurrentItem(0);
				break;
			case 1:
				viewpage_video.setBackgroundResource(R.drawable.viewpage_selected);
				viewpage_image.setBackgroundResource(R.drawable.viewpage_unselected);
				viewpage_terminal.setBackgroundResource(R.drawable.viewpage_unselected);
				mViewPager.setCurrentItem(1);
				break;
			case 2:
				viewpage_terminal.setBackgroundResource(R.drawable.viewpage_selected);
				viewpage_video.setBackgroundResource(R.drawable.viewpage_unselected);
				viewpage_image.setBackgroundResource(R.drawable.viewpage_unselected);
				mViewPager.setCurrentItem(2);
				break;
		}
	}
	
	/**
	 * 发送Handler消息
	 */
	public void sendHandlerMsg(int what) {
		Message msg = new Message();
		msg.what = what;
		handler.sendMessage(msg);
	}
	public void sendHandlerMsg(int what, int arg1) {
		Message msg = new Message();
		msg.what = what;
		msg.arg1 = arg1;
		handler.sendMessage(msg);
	}
	
	public Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
				//抓拍图片
				case INIT_LOCAL_IMAGE_FINISH:
					if (imageLocalFile == null || imageLocalFile.size() == 0) {
						if (noImageLayout != null)
							noImageLayout.setVisibility(View.VISIBLE);
						System.out.println("MyDebug: --------------> 更新抓拍图片");
						imageFileCount = 0;
						if (imageLocalFile != null) {
							imageLocalFile.clear();
						}
						if (imageListAdapter != null) {
							imageListAdapter.notifyDataSetChanged();
						}
						initViewPageView(0);
						initView();
						initLocalImageData();
						initLocalVideoData();
					} else {
						if (noImageLayout != null)
							noImageLayout.setVisibility(View.INVISIBLE);
						imageListAdapter = new ImageListViewAdapter(mActivity, imageLocalFile);
						if ((imageListAdapter != null) && (imageListView != null)) {
							imageListView.setAdapter(imageListAdapter);
						}
					}
					break;
				case NO_LOCAL_IMAGE_FILE:
					if (noImageLayout != null)
						noImageLayout.setVisibility(View.VISIBLE);
					break;
				//本地录像
				case INIT_LOCAL_VIDEO_FINISH:
					if (videoLocalFile == null || videoLocalFile.size() == 0) {
						if (noVideoLayout != null)
							noVideoLayout.setVisibility(View.VISIBLE);
						System.out.println("MyDebug: --------------> 更新本地录像");
						videoFileCount = 0;
						if (videoLocalFile != null) {
							videoLocalFile.clear();
						}
						if (videoListAdapter != null) {
							videoListAdapter.notifyDataSetChanged();
						}
						initViewPageView(1);
						initView();
						initLocalImageData();
						initLocalVideoData();
					} else {
						if (noVideoLayout != null)
							noVideoLayout.setVisibility(View.INVISIBLE);
						videoListAdapter = new VideoListViewAdapter(mActivity, videoLocalFile);
						if ((videoListAdapter != null) && (videoListView != null)) {
							videoListView.setAdapter(videoListAdapter);
						}
					}
					break;
				case NO_LOCAL_VIDEO_FILE:
					if (noVideoLayout != null)
						noVideoLayout.setVisibility(View.VISIBLE);
					break;
			}
		}
	};

	/**
	 * 排序文件夹集合，日期从大到小
	 */
	private ArrayList<Integer> sortImageFileName(File[] fileArray) {
		int fileCount = fileArray.length;
		ArrayList<Integer> arrayInteger = new ArrayList<Integer>();
		
		for (int i=0; i<fileCount; i++) {
			if (fileArray[i].getName().indexOf("-") <= 0)
				continue;
			Date date = Utils.StringToDate(fileArray[i].getName(), "yyyy-MM-dd");
			String fileString = Utils.DateToString(date, "yyyyMMdd");
			int fileInt = Integer.parseInt(fileString);
			arrayInteger.add(fileInt);
		}
		arrayInteger = Utils.bubbleSortArrayList(arrayInteger);
		return arrayInteger; 
	}
	
	/**
	 * 处理文件夹列表名字集合，排序日期从大到小的字符串数组
	 */
	private ArrayList<String> handleLocalFileName(File[] fileArray) {
		int fileCount = fileArray.length;
		if (fileCount == 0) {
			return null;
		}
		ArrayList<String> arrayString = new ArrayList<String>();
		ArrayList<Integer> arrayInteger = sortImageFileName(fileArray);
		fileCount = arrayInteger.size();
		
		for (int i=0; i<fileCount; i++) {
			String fileString = arrayInteger.get(i).toString();
			Date date = Utils.StringToDate(fileString, "yyyyMMdd");
			fileString = Utils.DateToString(date, "yyyy-MM-dd");
			arrayString.add(fileString);
		}
		return arrayString;
	}
	
	//-----------------------------------------------------------------------------------
	//抓拍图片处理
	//-----------------------------------------------------------------------------------
	private class LocalImageThread extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			initLocalImageData();
		}
	}
	
	/**
	 * 抓拍图片初始化
	 */
	private void initLocalImageData() {
		imageLocalFile = new ArrayList<LocalFileItem>();
		LocalFileItem fileItem = null;

		File rootPath = new File(SD_path);
		currentImageFile = rootPath;
		currentImageFileList = rootPath.listFiles();
		
		if (isImageDirectoryExist(currentImageFileList, "KaerVideo")) {
			if (isImageDirectoryExist(currentImageFileList, "image")) {
				
				ArrayList<String> imageFileStringArray = handleLocalFileName(currentImageFileList);
				if (imageFileStringArray == null) {
					sendHandlerMsg(NO_LOCAL_IMAGE_FILE);
				} else {
					imageFileCount = imageFileStringArray.size();
					if (imageFileCount == 0) {
						sendHandlerMsg(NO_LOCAL_IMAGE_FILE);
					} else {
						for (int i=0; i<imageFileCount; i++) {
							fileItem = new LocalFileItem();
							File file = new File(currentImageFile.getPath()+File.separator+imageFileStringArray.get(i));
							File[] files = file.listFiles();
							ArrayList<HashMap<String, Object>> fileImages = listAllImageViews(files);
							if (fileImages == null) {
								if (file.exists())
									file.delete();
								continue;
							}
							//文件夹名
							fileItem.fileName = imageFileStringArray.get(i);
							//文件夹下的图片
							fileItem.itemViews = fileImages;
							imageLocalFile.add(fileItem);
						}
						sendHandlerMsg(INIT_LOCAL_IMAGE_FINISH);
					}
				}
			} else {
				sendHandlerMsg(NO_LOCAL_IMAGE_FILE);
			}
		}
	}
	
	/**
	 * 查找文件夹集合下是否有这个文件夹
	 * @param files 文件夹集合
	 * @param fileName 要查找的文件夹
	 * @return true:有 false:没有
	 */
	private boolean isImageDirectoryExist(File[] files, String fileName) {
		for (int i=0; i<files.length; i++) {
			if (files[i].getName().equals(fileName)) {
				currentImageFile = files[i];
				currentImageFileList = currentImageFile.listFiles();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 将指定文件夹下的图片全部列出来
	 * @param files 文件集合
	 * @return 返回图片的文件集合
	 */
	private ArrayList<HashMap<String, Object>> listAllImageViews(File[] files) {
		if (files == null) {
			return null;
		}
		int count = files.length;
		if (count == 0) {
			return null;
		}
		ArrayList<HashMap<String, Object>> fileImages = new ArrayList<HashMap<String, Object>>();;
		HashMap<String, Object> imageItem = null;
		
		for (int i=0; i<count; i++) {
			if (!Utils.isImageFile(files[i].getName()))
				continue;
			imageItem = new HashMap<String, Object>();
			imageItem.put("imageInfo", files[i].getPath());
			fileImages.add(imageItem);
		}
		return fileImages;
	}
	
	//-----------------------------------------------------------------------------------
	//本地录像处理
	//-----------------------------------------------------------------------------------
	private class LocalVideoThread extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			initLocalVideoData();
		}
	}
	
	/**
	 * 本地录像初始化
	 */
	private void initLocalVideoData() {
		videoLocalFile = new ArrayList<LocalFileItem>();
		LocalFileItem fileItem = null;
		
		File rootPath = new File(SD_path);
		currentVideoFile = rootPath;
		currentVideoFileList = rootPath.listFiles();
		
		if (isVideoDirectoryExist(currentVideoFileList, "KaerVideo")) {
			if (isVideoDirectoryExist(currentVideoFileList, "video")) {
				
				ArrayList<String> videoFileStringArray = handleLocalFileName(currentVideoFileList);
				if (videoFileStringArray == null) {
					sendHandlerMsg(NO_LOCAL_VIDEO_FILE);
				} else {
					videoFileCount = videoFileStringArray.size();
					if (videoFileCount == 0) {
						sendHandlerMsg(NO_LOCAL_VIDEO_FILE);
					} else {
						for (int i=0; i<videoFileCount; i++) {
							fileItem = new LocalFileItem();
							File file = new File(currentVideoFile.getPath()+File.separator+videoFileStringArray.get(i));
							File[] files = file.listFiles();
							ArrayList<HashMap<String, Object>> fileVideos = listAllVideoRecords(file, files);
							if (fileVideos == null) {
								continue;
							}
							//文件夹名
							fileItem.fileName = videoFileStringArray.get(i);
							//文件夹下的录像
							fileItem.itemViews = fileVideos;
							videoLocalFile.add(fileItem);
						}
						sendHandlerMsg(INIT_LOCAL_VIDEO_FINISH);
					}
				}
			} else {
				sendHandlerMsg(NO_LOCAL_VIDEO_FILE);
			}
		}
	}
	
	/**
	 * 查找文件夹集合下是否有这个文件夹
	 * @param files 文件夹集合
	 * @param fileName 要查找的文件夹
	 * @return true:有 false:没有
	 */
	private boolean isVideoDirectoryExist(File[] files, String fileName) {
		for (int i=0; i<files.length; i++) {
			if (files[i].getName().equals(fileName)) {
				currentVideoFile = files[i];
				currentVideoFileList = currentVideoFile.listFiles();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 将指定文件夹下的录像全部列出来
	 * @param files 文件集合
	 * @return 返回录像的文件集合
	 */
	private ArrayList<HashMap<String, Object>> listAllVideoRecords(File file, File[] fileList) {
		if (fileList == null) {
			return null;
		}
		int recordsCount = 0;
		int listCount = fileList.length;
		if (listCount == 0) {
			return null;
		}
		ArrayList<HashMap<String, Object>> fileVideos = new ArrayList<HashMap<String, Object>>();;
		HashMap<String, Object> videoItem = null;
		
		boolean isExists = true;
		File[] thumbnailsList = null;
		int thumbnailsCount = 0;
		File thumbnails = new File(file.getPath() + File.separator + "thumbnails");
		if (thumbnails.exists()) {
			thumbnailsList = thumbnails.listFiles();
			thumbnailsCount = thumbnailsList.length;
			if (thumbnailsCount == 0) {
				Utils.deleteFile(file);
				return null;
			}
			isExists = true;
		} else {
			isExists = false;
		}
		
		for (int i=0; i<listCount; i++) {
			if (!Utils.isVideoFile(fileList[i].getName()))
				continue;
			recordsCount ++;
			videoItem = new HashMap<String, Object>();
			videoItem.put("videoFile", file.getPath());
			videoItem.put("videoPath", fileList[i].getPath());
			if (isExists) {
				
				boolean isHaveThumbnails = false;
				String fileListItemName = fileList[i].getName();
				String thumbnailsName = fileListItemName.substring(0, fileListItemName.lastIndexOf(".")).toLowerCase();

				for (int j=0; j<thumbnailsCount; j++) {
					String name = thumbnailsList[j].getName().substring(0, thumbnailsList[j].getName().lastIndexOf(".")).toLowerCase();
					if (name.equals(thumbnailsName)) {
						isHaveThumbnails = true;
						videoItem.put("videoBg", thumbnailsList[j].getPath());
						break;
					}
				}
				if (isHaveThumbnails) {
					isHaveThumbnails = false;
				} else {
					videoItem.put("videoBg", "null");
				}
			} else {
				videoItem.put("videoBg", "null");
			}
			fileVideos.add(videoItem);
		}
		if (recordsCount == 0) {
			Utils.deleteFile(file);
		}
		return fileVideos;
	}
	
	//-----------------------------------------------------------------------------------
	//终端录像处理
	//-----------------------------------------------------------------------------------
	private class OnItemClickListenerImpl implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
			// TODO Auto-generated method stub
			//请求终端录像
			Intent intent = new Intent(mActivity, SetDateActivity.class);
			intent.putExtra("dealerName", deviceList.get(position).get("dealerName"));
			startActivity(intent);
			mActivity.overridePendingTransition(R.anim.right_in, R.anim.fragment_nochange);
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//注销广播
		mActivity.unregisterReceiver(localReceiver);
	}

	public class LocalReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			
			if (action.equals(REFRESH_VIDEO_FILE)) {
				//本地录像初始化
				new LocalVideoThread().start();
			}
		}
	}
}

