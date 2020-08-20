package javacvtest;

import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

/**
 * Mp4文件和Flv文件格式互转
 * 已经测试成功
 */
public class Mp4FileAndFlvFileTransfer {
    public static void recode() throws FrameGrabber.Exception, FrameRecorder.Exception {
        String filePath = "E:\\meeting.mp4";
        String ext = filePath.substring(filePath.lastIndexOf("."));
        String newFilePath = filePath.replace(ext, "_recode.mp4");
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(filePath);
        grabber.start();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(newFilePath, grabber.getImageWidth(),
                grabber.getImageHeight(), grabber.getAudioChannels());
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("mp4");
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        recorder.setFrameRate(grabber.getFrameRate());
        int bitrate = grabber.getVideoBitrate();
        if (bitrate == 0) {
            bitrate = grabber.getAudioBitrate();
        }
        recorder.setVideoBitrate(bitrate);
        recorder.start(grabber.getFormatContext());
        AVPacket packet;
        long dts = 0;
        while ((packet = grabber.grabPacket()) != null) {
            long currentDts = packet.dts();
            if (currentDts >= dts) {
                recorder.recordPacket(packet);
            }
            dts = currentDts;
        }
        recorder.stop();
        recorder.release();
        grabber.stop();
    }
    public static void main(String[] args) throws FrameGrabber.Exception, FrameRecorder.Exception {
        Mp4FileAndFlvFileTransfer third = new Mp4FileAndFlvFileTransfer();
        third.recode();
    }
}
