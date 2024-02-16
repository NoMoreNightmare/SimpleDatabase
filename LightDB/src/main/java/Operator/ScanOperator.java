package Operator;

import Tuple.Tuple;
import ed.inf.adbs.lightdb.LightDB;

public class ScanOperator implements Operator{

    //TODO 应该在构造的时候知道要解析的sql文件路径，数据文件的路径和最终输出的路径
    public ScanOperator(String filename){

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

    //TODO 把tuple写到适合的printStream，比如文件或者输出控制台
    @Override
    public void dump() {
        String printChoice = LightDB.properties.getProperty("printStream");
        if ("file".equals(printChoice)){

        }else if("console".equals(printChoice)){

        }else{
            //TODO 默认输出到控制台
        }
    }
}
