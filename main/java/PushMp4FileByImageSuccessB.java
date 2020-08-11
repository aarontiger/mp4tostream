
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;

public class PushMp4FileByImageSuccessB {
    static boolean exit  = false;
    public static void main(String[] args) throws Exception {
        System.out.println("start...");
        String rtmpPath = "rtmp://192.168.66.162:1935/live/haotestA1";
        String mediaFileName = "E://meeting.mp4";

        int audioRecord =0; // 0 = 不录制，1=录制
        boolean saveVideo = false;
        push(rtmpPath,mediaFileName,audioRecord,saveVideo);

        System.out.println("end...");
    }
    public static void push(String rtmpPath,String mediaFileName,int audioRecord,boolean saveVideo ) throws Exception  {
        try {
            // 使用rtsp的时候需要使用 FFmpegFrameGrabber，不能再用 FrameGrabber
            int width = 640, height = 480;
            FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(mediaFileName);
            //grabber.setOption("rtsp_transport", "tcp"); // 使用tcp的方式，不然会丢包很严重

            grabber.setImageWidth(width);
            grabber.setImageHeight(height);
            System.out.println("grabber start");
            avutil.av_log_set_level(avutil.AV_LOG_ERROR);
            FFmpegLogCallback.set();
            grabber.start();
            // 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(rtmpPath, width, height, audioRecord);
            recorder.setInterleaved(true);
            //recorder.setVideoOption("crf","28");
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // 28
            recorder.setFormat("flv"); // rtmp的类型    //TO-DO to research
            recorder.setFrameRate(25);
            recorder.setImageWidth(width);
            recorder.setImageHeight(height);
            recorder.setPixelFormat(0); // yuv420p
            System.out.println("recorder start");
            AVFormatContext fc = null;
            fc = grabber.getFormatContext();
            recorder.start(fc);
            //recorder.start();
            //
            //OpenCVFrameConverter.ToIplImage conveter = new OpenCVFrameConverter.ToIplImage();
            System.out.println("all start!!");
            int count = 0;
      /*  while(!exit){
            count++;
            Frame frame = grabber.grabImage();
            if(frame == null){
                continue;
            }
            if(count % 100 == 0){
                System.out.println("count="+count);
            }
            recorder.record(frame);
        }*/
            AVPacket packet;
            long dts = 0;
            while ((packet = grabber.grabPacket()) != null && !exit) {
                count++;
                long currentDts = packet.dts();
                if (currentDts >= dts) {
                    recorder.recordPacket(packet);
                }
                dts = currentDts;
                if (count % 100 == 0) {
                    System.out.println("count=" + count);
                }
            }

            grabber.stop();
            grabber.release();
            recorder.stop();
            recorder.release();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
