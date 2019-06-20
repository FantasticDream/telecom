package com.zc.ct.analysis;

import com.zc.ct.analysis.tool.AnalysisTextTool;
import org.apache.hadoop.util.ToolRunner;

/**
 * 分析数据
 */
public class AnalysisData {

    public static void main(String[] args) throws Exception{
        ToolRunner.run(new AnalysisTextTool(),args);
    }
}
