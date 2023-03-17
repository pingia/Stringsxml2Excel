package com.github.pingia;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.util.ListUtils;

/**
 * Hello world!
 *
 */
public class TranslateAndroidResources 
{

    final static String sheet_name = "模板";

    final static String output_dir = "d:\\xml_excel";
    
    public static void main( String[] args )
    {
        System.out.println( "========Android字符串资源处理程序启动======!\n" );
        File root = new File(TranslateAndroidResources.class.getResource("/").getPath());

        //过滤所有xml文件
        File[] xmlFilesArray = root.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                // TODO Auto-generated method stub
                if(name.endsWith("xml")) return true;
                return false;
            }

        });

        //过滤所有excel文件
        File[] excelFilesArray = root.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                // TODO Auto-generated method stub
                if(name.endsWith("xls") || name.endsWith("xlsx")) return true;
                return false;
            }
            
        });

        for(File file : xmlFilesArray){
            xml2Excel(file.getPath());
        }

        for(File file : excelFilesArray){
            excel2Xml(file.getPath());
        }
    }

    /**
     * 获取文件名的不带后缀名称
     * @param fileName
     * @return
     */
    private static String getNoSuffixFileName(String fileName){
        return fileName.substring(0, fileName.indexOf("."));

    }
        

    /**
     * 
     * @param xmlFilePath
     */
    public static void xml2Excel(String xmlFilePath){
                
        try{
            System.out.println("准备读入的xml文件路径: " + xmlFilePath);

            File xmlFile = new File(xmlFilePath);
            String xmlFileNameWithSuffix = xmlFile.getName();
            System.out.println("准备读入的xml带后缀文件名:" + xmlFileNameWithSuffix);

        //1.创建Reader对象
            SAXReader reader = new SAXReader();
            reader.setEncoding("utf-8");
         

            //2.加载xml
            Document document = reader.read(xmlFile);
            //3.获取根节点
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();

            String xmlFileNameNoSuffix = getNoSuffixFileName(xmlFileNameWithSuffix);
           

            if(!new File(output_dir).exists()){
                new File(output_dir).mkdirs();
            }

            File  outputExcelFile = new File(output_dir, xmlFileNameNoSuffix + "_excel.xlsx");

            List<AndroidStringData> list = new ArrayList<AndroidStringData>();
            while (iterator.hasNext()){
                Element stringElement = (Element) iterator.next();

                

                if(!stringElement.getName().equalsIgnoreCase("string")) continue;

                Attribute tranAttr = stringElement.attribute("translatable");

                if(tranAttr != null && "false".equals(tranAttr.getValue())){  //如果指明不需要翻译，那么就跳过，
                    continue; 
                }

                Attribute attr = stringElement.attribute("name");
                String strValue = attr.getValue();

                

                AndroidStringData d = new AndroidStringData();
                d.setName(strValue);

                List<Element> subElements = stringElement.elements();

                StringBuffer sb = new StringBuffer();
                if(subElements.size() > 0){
                    for (Element element : subElements) {
                        sb.append(element.asXML());
                    }

                    d.setValue(sb.toString());

                    System.out.println("节点 : " + stringElement.getName() +"有子节点，直接处理成xml字符串：" + sb.toString());
                    
                }else{

                       
                    d.setValue(   stringElement.getStringValue());    //这里比较坑爹，xml里的转义符会被还原成原字符，最后才写入excel
                }
                list.add(d);

            }

            System.out.println(xmlFilePath +", 总共读到待翻译的字符串个数: " + list.size());

            EasyExcel.write(outputExcelFile, AndroidStringData.class).sheet(sheet_name).doWrite(list);

            System.out.println("写入全部xml中要翻译的字符串到excel文件，成功。。。。");

        }catch(Exception e){
            e.printStackTrace();
            System.out.println("写入全部xml中要翻译的字符串到excel文件，失败，原因：" + e.getLocalizedMessage());
        }

        
    }

    public static void excel2Xml(final String excelFilePath){
            // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        try{
            System.out.println("准备读入的翻译后excel路径是 : " + excelFilePath);

            File excelFile =new File(excelFilePath);
            final String excelFileNameWithSuffix = excelFile.getName();
            System.out.println("准备读入的翻译后excel带后缀文件名是: " + excelFileNameWithSuffix);

            String excelFileNameNoSuffix = getNoSuffixFileName(excelFileNameWithSuffix);
                        
            if(!new File(output_dir).exists()){
                new File(output_dir).mkdirs();
            }

            final File outputXmlFile = new File(output_dir,  excelFileNameNoSuffix +"_xml.xml");
            if(outputXmlFile.exists()) outputXmlFile.delete();
            
            EasyExcel.read(excelFile, AndroidStringData.class, new  com.alibaba.excel.read.listener.ReadListener<AndroidStringData>() {

                /**
                 * 单次缓存的数据量
                 */
                public static final int BATCH_COUNT = 100;
                /**
                 *临时存储
                */
                private List<AndroidStringData> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);


                public void doAfterAllAnalysed(AnalysisContext arg0) {
                    parseExcelFileData(excelFilePath);
                    System.out.println(excelFilePath + ", 所有excel数据解析完成！");
                }

                public void invoke(AndroidStringData data, AnalysisContext arg1) {
                    cachedDataList.add(data);
                    if (cachedDataList.size() >= BATCH_COUNT) {
                        parseExcelFileData(excelFilePath);
                        // 存储完成清理 list
                        cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
                    }
                
                }

            
                private void parseExcelFileData(String excelFilePath) {
                    System.out.println(excelFilePath + "读到的翻译后excel行数是 ： " + cachedDataList.size());
            
                    try{
                        Document document;
                        Element rootElement = null;
                        if(outputXmlFile.exists()){
                            System.out.println("注意：输出xml："  + outputXmlFile.getName() + "已存在，准备进行追加写入...");
                            SAXReader reader = new SAXReader();
                            // reader.setEncoding("utf-8");
                            document = reader.read(outputXmlFile);

                            if(null != document){
                                rootElement = document.getRootElement();

                                if(!rootElement.getName().equalsIgnoreCase("resources")){
                                    System.out.println("虽然"  + outputXmlFile.getName() + "已存在，但是根节点不是resources,因此进行覆盖写入...");
                                    document =DocumentHelper.createDocument();
                                    rootElement = document.addElement("resources");
                                }
                            }
                        }else{
                            System.out.println("输出文件: " + outputXmlFile.getName() + "不存在，进行写入...");
                            document =DocumentHelper.createDocument();
                            rootElement = document.addElement("resources");
                        }

                        for(AndroidStringData data : cachedDataList){
                            
                            String translate_value = data.getTranslationValue();
                            String name = data.getName();
                            System.out.println("字符串名是:" + name +",翻译值是:  " + translate_value );

                            if(null != rootElement) {
                                Element stringElement = rootElement.addElement("string");
                                stringElement.setText(translate_value == null ? "" : translate_value);
                                stringElement.addAttribute("name", name);
                            }

                            
                        }




                        OutputFormat format = OutputFormat.createPrettyPrint();
                        //设置编码格式为gbk，默认utf-8
                        format.setEncoding("utf-8");
                        Writer out;
                        // format.setOmitEncoding(true);
                        out = new OutputStreamWriter(new FileOutputStream(outputXmlFile),"UTF-8");  //写入非ascii码字符时，这里必须是找个写法
                        XMLWriter writer = new XMLWriter(out, format);
                        writer.setEscapeText(false);    //设置是否转义。设置不转义
                        writer.write(document);
                        writer.close();

                        System.out.println("写入全部excel中要还原到android的字符串到xml文件，成功。。。。");
                
                    }catch(Exception e){
                        e.printStackTrace();
                        System.out.println("写入全部excel中要还原到android的字符串到xml文件，失败，原因：" + e.getLocalizedMessage());
                    }
            
                }
                
            }).sheet(sheet_name).doRead();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

}
