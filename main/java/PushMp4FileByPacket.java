import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.*;

public class PushMp4FileByPacket {
    static boolean exit  = false;
    public static void main(String[] args) throws Exception {
        System.out.println("start...");
        String rtmpPath = "rtmp://127.0.0.1:1935/live/haotestA2";
        String mediaFileName = "E://meeting.mp4";

        int audioRecord =0; // 0 = 不录制，1=录制
        boolean saveVideo = false;
        //push(rtmpPath,mediaFileName,audioRecord,saveVideo);
        pushByPacket(rtmpPath,mediaFileName,audioRecord,saveVideo);

        System.out.println("end...");
    }
        public static void pushByPacket(String rtmpPath,String mediaFileName,int audioRecord,boolean saveVideo ) throws Exception  {
        // 使用rtsp的时候需要使用 FFmpegFrameGrabber，不能再用 FrameGrabber
        int width = 640,height = 480;
        FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(mediaFileName);
        //grabber.setOption("rtsp_transport", "tcp"); // 使用tcp的方式，不然会丢包很严重


                grabber.setImageWidth(width);
                grabber.setImageHeight(height);
                System.out.println("grabber start");

                grabber.start();
                // 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
                FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(rtmpPath,
                        grabber.getImageWidth(), grabber.getImageHeight(), 0);
            try {
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
                AVFormatContext fc = null;
                fc = grabber.getFormatContext();
                recorder.start(fc);
                // 清空探测时留下的缓存
                //grabber.flush();


                System.out.println("recorder start");
                //recorder.start();

                int count = 0;
                ///////


                /////
                AVPacket packet;
                long dts = 0;
                while ((packet = grabber.grabPacket()) != null && !exit) {
                    long currentDts = packet.dts();
                    if (currentDts >= dts) {
                        recorder.recordPacket(packet);
                    }
                    dts = currentDts;
                    if (count % 100 == 0) {
                        System.out.println("count=" + count);
                    }
                }
                recorder.stop();
                recorder.release();
                grabber.stop();

                /////
            }catch (Exception e)
            {
                System.out.println("hao error");

                e.printStackTrace();
            }

        grabber.stop();
        grabber.release();
        recorder.stop();
        recorder.release();
    }
}
