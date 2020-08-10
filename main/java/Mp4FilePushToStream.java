



import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import static org.bytedeco.javacpp.avcodec.av_packet_unref;


public class Mp4FilePushToStream {

/**
    public static void main(String[] args) {
         Mp4FilePushToStream util = new Mp4FilePushToStream();
         util.pushMp4ToStream();
    }
        public void pushMp4ToStream(){
        int err_index = 0;///推流过程中出现错误的次数
        try {
          InputStream in = new FileInputStream("E:\\we-are-so-happy1000.mp4");
          FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(in, 0);
//            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(in);
            grabber.setOption("stimeout", "2000000");
            grabber.setVideoOption("vcodec", "copy");
            grabber.setFormat("mpeg");
            grabber.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            // h264编/解码器
            grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);



            grabber.start();

            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtmp://127.0.0.1:1935/live/mp4test",
                    grabber.getImageWidth(), grabber.getImageHeight(), 0);
            recorder.setInterleaved(true);
            // 设置比特率
            recorder.setVideoBitrate(2500000);
            // h264编/解码器
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            // 封装flv格式
            recorder.setFormat("flv");
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            // 视频帧率(保证视频质量的情况下最低25，低于25会出现闪屏)
            recorder.setFrameRate(grabber.getFrameRate());
            // 关键帧间隔，一般与帧率相同或者是视频帧率的两倍
            recorder.setGopSize((int) grabber.getFrameRate() * 2);
            avformat.AVFormatContext fc = null;
            fc = grabber.getFormatContext();
            recorder.start(fc);
            // 清空探测时留下的缓存
            grabber.flush();

            org.bytedeco.javacpp.avcodec.AVPacket pkt = null;
            long dts = 0;
            long pts = 0;

            System.out.println("开始推流");
            for (int no_frame_index = 0; no_frame_index < 5 || err_index < 5;) {
                pkt = grabber.grabPacket();
                if (pkt == null || pkt.size() <= 0 || pkt.data() == null) {
                    // 空包记录次数跳过
                    no_frame_index++;
                    err_index++;
                    continue;
                }
                // 获取到的pkt的dts，pts异常，将此包丢弃掉。
                if (pkt.dts() == avutil.AV_NOPTS_VALUE && pkt.pts() == avutil.AV_NOPTS_VALUE || pkt.pts() < dts) {
                    err_index++;
                    av_packet_unref(pkt);
                    continue;
                }
                // 记录上一pkt的dts，pts
                dts = pkt.dts();
                pts = pkt.pts();
                // 推数据包
                err_index += (recorder.recordPacket(pkt) ? 0 : 1);
                // 将缓存空间的引用计数-1，并将Packet中的其他字段设为初始值。如果引用计数为0，自动的释放缓存空间。
                av_packet_unref(pkt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("推流结束");
        }
    }**/
}
