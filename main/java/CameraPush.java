
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Timer;

import static org.bytedeco.ffmpeg.global.avcodec.av_packet_unref;

/**
 * @Title CameraPush.java
 * @description 拉流推流
 * @time 2019年12月16日 上午9:34:41
 * @author wuguodong
 **/
public class CameraPush {

	private final static Logger logger = LoggerFactory.getLogger(CameraPush.class);
	private static String mediaFileName ="E://meeting.mp4";
	private static String rtmpUrl="rtmp:/127.0.0.1:1935/live/haotestA1";


	protected FFmpegFrameGrabber grabber = null;// 解码器
	protected FFmpegFrameRecorder record = null;// 编码器
	int width;// 视频像素宽
	int height;// 视频像素高

	// 视频参数
	protected int audiocodecid;
	protected int codecid;
	protected double framerate;// 帧率
	protected int bitrate;// 比特率

	// 音频参数
	// 想要录制音频，这三个参数必须有：audioChannels > 0 && audioBitrate > 0 && sampleRate > 0
	private int audioChannels;
	private int audioBitrate;
	private int sampleRate;

	public CameraPush() {
		super();
	}


	/**
	 * 选择视频源
	 * 
	 * @author wuguodong
	 * @throws org.bytedeco.javacv.FrameGrabber.Exception
	 * @throws org.bytedeco.javacv.FrameGrabber.Exception
	 * @throws org.bytedeco.javacv.FrameGrabber.Exception
	 * @throws Exception
	 */
	public CameraPush from() throws Exception {
		// 采集/抓取器
		grabber = new FFmpegFrameGrabber(mediaFileName);



		try {
			grabber.start();

			logger.debug("******   grabber.start()    END     ******");

			// 开始之后ffmpeg会采集视频信息，之后就可以获取音视频信息
			width = grabber.getImageWidth();
			height = grabber.getImageHeight();
			// 若视频像素值为0，说明拉流异常，程序结束
			if (width == 0 && height == 0) {
				logger.error( "  拉流异常！");
				grabber.stop();
				grabber.close();
				return null;
			}
			// 视频参数

			codecid = grabber.getVideoCodec();
			framerate = grabber.getVideoFrameRate();// 帧率
			bitrate = grabber.getVideoBitrate();// 比特率
			// 音频参数
			audiocodecid = grabber.getAudioCodec();
			// 想要录制音频，这三个参数必须有：audioChannels > 0 && audioBitrate > 0 && sampleRate > 0
			audioChannels = grabber.getAudioChannels();
			audioBitrate = grabber.getAudioBitrate();
			if (audioBitrate < 1) {
				audioBitrate = 128 * 1000;// 默认音频比特率
			}
		} catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
			logger.error("ffmpeg错误信息：", e);
			grabber.stop();
			grabber.close();
			return null;
		}

		return this;
	}

	/**
	 * 选择输出
	 * 
	 * @author wuguodong
	 * @throws Exception
	 */
	public CameraPush to() throws Exception {
		// 录制/推流器
		record = new FFmpegFrameRecorder(rtmpUrl, width, height);
		record.setVideoOption("crf", "28");// 画面质量参数，0~51；18~28是一个合理范围
		record.setGopSize(2);
		record.setFrameRate(framerate);
		record.setVideoBitrate(bitrate);

		record.setAudioChannels(audioChannels);
		record.setAudioBitrate(audioBitrate);
		record.setSampleRate(sampleRate);
		AVFormatContext fc = null;
		if (rtmpUrl.indexOf("rtmp") >= 0 || rtmpUrl.indexOf("flv") > 0) {
			// 封装格式flv
			record.setFormat("flv");
			record.setAudioCodecName("aac");
			record.setVideoCodec(27);
			fc = grabber.getFormatContext();
		}
		try {
			record.start(fc);
		} catch (Exception e) {
			logger.error("  推流异常！");
			logger.error("ffmpeg错误信息：", e);
			e.printStackTrace();
			grabber.stop();
			grabber.close();
			record.stop();
			record.close();
			return null;
		}
		return this;

	}

	/**
	 * 转封装
	 * 
	 * @author wuguodong
	 * @throws org.bytedeco.javacv.FrameGrabber.Exception
	 * @throws org.bytedeco.javacv.FrameRecorder.Exception
	 * @throws InterruptedException
	 */
	public CameraPush go()
			throws org.bytedeco.javacv.FrameGrabber.Exception, org.bytedeco.javacv.FrameRecorder.Exception {
		long err_index = 0;// 采集或推流导致的错误次数
		// 连续五次没有采集到帧则认为视频采集结束，程序错误次数超过5次即中断程序
		logger.info( " 开始推流...");
		// 释放探测时缓存下来的数据帧，避免pts初始值不为0导致画面延时
		grabber.flush();
		for (int no_frame_index = 0; no_frame_index < 5 || err_index < 5;) {
			try {
				// 用于中断线程时，结束该循环
				Thread.sleep(1);
				AVPacket pkt = null;
				// 获取没有解码的音视频帧
				pkt = grabber.grabPacket();
				if (pkt == null || pkt.size() <= 0 || pkt.data() == null) {
					// 空包记录次数跳过
					no_frame_index++;
					err_index++;
					continue;
				}
				// 不需要编码直接把音视频帧推出去
				err_index += (record.recordPacket(pkt) ? 0 : 1);

				av_packet_unref(pkt);
			} catch (InterruptedException e) {
				// 销毁构造器
				grabber.stop();
				grabber.close();
				record.stop();
				record.close();
				logger.info( " 中断推流成功！");
				break;
			} catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
				err_index++;
				e.printStackTrace();
			} catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
				err_index++;
				e.printStackTrace();
			}
		}
		// 程序正常结束销毁构造器
		grabber.stop();
		grabber.close();
		record.stop();
		record.close();
		logger.info( " 推流结束...");
		return this;
	}
	public static void main(String[] args)  {
		try {
			CameraPush cameraPush = new CameraPush();
			cameraPush.from();
			cameraPush.to();
			cameraPush.go();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
