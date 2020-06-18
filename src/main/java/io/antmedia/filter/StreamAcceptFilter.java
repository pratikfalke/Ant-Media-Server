package io.antmedia.filter;

import org.bytedeco.javacpp.avcodec.AVPacket;
import org.bytedeco.javacpp.avformat.AVFormatContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import io.antmedia.AppSettings;

public class StreamAcceptFilter implements ApplicationContextAware{

	private AppSettings appSettings;

	protected static Logger logger = LoggerFactory.getLogger(StreamAcceptFilter.class);
	
	public boolean isValidStreamParameters(AVFormatContext inputFormatContext,AVPacket pkt) 
	{
		return  checkFPSAccept(getStreamFps(inputFormatContext, pkt)) && 
				checkResolutionAccept(getStreamResolution(inputFormatContext, pkt)) &&
				checkBitrateAccept(getStreamBitrate(inputFormatContext, pkt));
	}

	public boolean checkFPSAccept(int streamFPSValue) 
	{
		if(getMaxFps() > 0 && getMaxFps()  < streamFPSValue) {
			logger.error("Exceeding Max FPS({}) limit. FPS is: {}", getMaxFps(), streamFPSValue);
			return false;
		}		
		return true;
	} 

	public boolean checkResolutionAccept(int streamResolutionValue) 
	{
		if (getMaxResolution() > 0 && getMaxResolution() < streamResolutionValue) {
			logger.error("Exceeding Max Resolution({}) acceptable limit. Resolution is: {}", getMaxResolution(), streamResolutionValue);
			return false;
		}
		return true;

	} 

	public boolean checkBitrateAccept(long streamBitrateValue) 
	{
		if (getMaxBitrate() > 0 && getMaxBitrate() < streamBitrateValue) {
			logger.error("Exceeding Max Bitrate({}) acceptable limit. Stream Bitrate is: {}", getMaxBitrate(), streamBitrateValue);
			return false;
		}
		return true;
	} 

	public int getStreamFps(AVFormatContext inputFormatContext,AVPacket pkt) 
	{
		int streamFPSValue = (inputFormatContext.streams(pkt.stream_index()).r_frame_rate().num()) / (inputFormatContext.streams(pkt.stream_index()).r_frame_rate().den());
		logger.info("Stream FPS value: {}",streamFPSValue);

		return streamFPSValue;
	}

	public int getStreamResolution(AVFormatContext inputFormatContext,AVPacket pkt) {
		int streamResolutionValue = inputFormatContext.streams(pkt.stream_index()).codecpar().height();
		logger.error("Stream Resolution value: {}",streamResolutionValue);

		return streamResolutionValue;
	}

	public long getStreamBitrate(AVFormatContext inputFormatContext,AVPacket pkt) {
		long streamBitrateValue = inputFormatContext.streams(pkt.stream_index()).codecpar().bit_rate();
		logger.error("Stream Bitrate value: {}",streamBitrateValue);

		return streamBitrateValue;		
	}


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		if (applicationContext.containsBean(AppSettings.BEAN_NAME)) {
			appSettings = (AppSettings)applicationContext.getBean(AppSettings.BEAN_NAME);
		}
	}
	

	public AppSettings getAppSettings() {
		return appSettings;
	}
	
	public int getMaxFps() {
		if (appSettings != null) {
			return appSettings.getMaxFpsAccept();
		}
		return 0;
	}
	
	public int getMaxResolution() {
		if (appSettings != null) {
			return appSettings.getMaxResolutionAccept();
		}
		return 0;
	}

	public int getMaxBitrate() {
		if (appSettings != null) {
			return appSettings.getMaxBitrateAccept();
		}
		return 0;
	}
	
}
