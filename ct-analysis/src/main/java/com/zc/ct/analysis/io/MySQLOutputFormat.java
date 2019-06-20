package com.zc.ct.analysis.io;

import com.zc.ct.common.util.JDBCUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Mysql格式化输出对象
 */
public class MySQLOutputFormat extends OutputFormat<Text, Text> {

    protected static class MySQLRecorderWriter extends RecordWriter<Text,Text>{

        private Connection connection = null;

        public MySQLRecorderWriter(){
            connection = JDBCUtil.getConnection();
        }

        /**
         * 输出数据
         * @param text
         * @param text2
         * @throws IOException
         * @throws InterruptedException
         */

        @Override
        public void write(Text text, Text text2) throws IOException, InterruptedException {
            if(connection!=null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 释放资源
         * @param taskAttemptContext
         * @throws IOException
         * @throws InterruptedException
         */

        @Override
        public void close(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {

        }
    }

    @Override
    public RecordWriter<Text, Text> getRecordWriter(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        return new MySQLRecorderWriter();
    }

    @Override
    public void checkOutputSpecs(JobContext jobContext) throws IOException, InterruptedException {

    }


    private FileOutputCommitter committer = null;
    public static Path getOutputPath(JobContext job){
        String name = job.getConfiguration().get(FileOutputFormat.OUTDIR);
        return name == null ? null : new Path(name);
    }


    @Override
    public OutputCommitter getOutputCommitter(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        if (committer == null){
            Path output = getOutputPath(taskAttemptContext);
            committer = new FileOutputCommitter(output,taskAttemptContext);
        }
        return committer;
    }
}
