package Operator;

import pojo.Tuple;

public class ScanOperator extends Operator{

    //TODO 创建一个能够读取csv文件的对象引用





    //TODO 应该在构造的时候知道要解析的sql文件路径，数据文件的路径和最终输出的路径
    public ScanOperator(String tableName){
        //TODO 得到表名

        //TODO 将dbPath和表名拼接，获取数据文件的路径
            //TODO 读取数据文件，创建读取文件的对象

        //TODO 存储outputPath
    }

    //TODO 获取这个operator会输出的下一个tuple
    @Override
    public Tuple getNextTuple() {
        return null;
    }

    //TODO 从头开始，从这个operator会返回的第一个tuple重新开始
    @Override
    public void reset() {

    }


}
