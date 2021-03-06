package com.xkd.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static java.lang.System.*;

//gaodd
public class FileUtil {

	public final  static  int MAX_EXCELL_ROWS = 65536;
	/**
	 * 新建目录
	 * 
	 * @param folderPath
	 *            String 如 c:/fqf
	 * @return boolean
	 */
	public static String  newFolder(String folderPath) {
		try {
			String filePath = folderPath;
			filePath = filePath.toString();
			File myFilePath = new File(filePath);
			
			System.out.println("创建中："+myFilePath.exists());
			if (!myFilePath.exists()) {
				myFilePath.mkdir();
				System.out.println("创建 "+folderPath+" 文件夹");
				String os = System.getProperty("os.name");
				if (!os.toLowerCase().startsWith("win")) {
					try {
						Runtime.getRuntime().exec("chmod 777 " + myFilePath.getPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}else{
				return "error";
			}
		} catch (Exception e) {
			System.out.println("新建目录操作出错");
			e.printStackTrace();
		}
		return "success";
	}

	/**
	 * 修改文件夹名称
	 * 
	 * @param oldPathFolerName
	 *            String 如 f:/aa123123 newsPathFolerName String 如 f:/aa
	 * 
	 * @return true 成功 flase 失败
	 */
	public static Boolean renameFoler(String oldPathFolerName, String newsPathFolerName) {

		File oldFile = new File(oldPathFolerName);
		if (!oldFile.exists()) {
			try {
				oldFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// String rootPath = oldFile.getParent();
		File newFile = new File(newsPathFolerName);
		if (oldFile.renameTo(newFile)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 修改文件名称
	 * 
	 * @param oldPathFolerName
	 *            String 如 F:\\aa11\\32444.xlsx newsPathFileName String 如
	 *            F:\\aa11\\123123.xlsx
	 * 
	 * @return true 成功 flase 失败
	 */
	public static void main(String[] args) {
		renameFile("F://aa/112.xlsx",
				"F://wendanguanli/0/b6e3264a-7b00-45e0-a4df-3ee6ac31f8f9/b6e3264a-7b00-45e0-a4df-3ee6ac31f8f9__0/5bc08b80-0e2c-4a00-b7fc-5c1bf2bf67ce/a.xlsx");
	}
	public static Boolean renameFile(String oldPathFileName, String newsPathFileName) {

		File toBeRenamed = new File(oldPathFileName);
		// 检查要重命名的文件是否存在，是否是文件
		if (!toBeRenamed.exists() || toBeRenamed.isDirectory()) {
			return false;
		}
		File newFile = new File(newsPathFileName);
		if (toBeRenamed.renameTo(newFile)) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 保存文件
	 * 
	 * @param folderPath
	 *            String 如 c:/fqf
	 * @return boolean
	 */

	public static String saveFile(MultipartFile file, String folderAndFilePath) {
		try {

			File targetFile = new File(folderAndFilePath); // 新建文件
			
			System.out.println("新建文件，文件存在否="+targetFile.exists());
			if (!targetFile.exists()) {
				String os = System.getProperty("os.name");
				file.transferTo(targetFile); // 传送 失败就抛异常
				targetFile.setExecutable(true);// 设置可执行权限
				targetFile.setReadable(true);// 设置可读权限
				targetFile.setWritable(true);// 设置可写权限
				String saveFilename = targetFile.getPath();
				if (!os.toLowerCase().startsWith("win")) {
					Runtime.getRuntime().exec("chmod 777 " + saveFilename);
				}
				FileInputStream fileInputStream= new FileInputStream(targetFile);
				long size = fileInputStream.getChannel().size();
				fileInputStream.close();
				return size+"";
			} else {
				return "error";// 文件已存在
			}
		} catch (Exception e) {
			System.out.println("保存文件");
			e.printStackTrace();
			return "fail";
		}
	}

	/**
	 * 删除文件
	 * 
	 * @param filePathAndName
	 *            String 文件路径及名称 如c:/fqf.txt
	 * @param fileContent
	 *            String
	 * @return boolean
	 */
	public static void delFile(String filePathAndName) {
		try {
			String filePath = filePathAndName;
			filePath = filePath.toString();
			File myDelFile = new File(filePath);
			myDelFile.delete();

		} catch (Exception e) {
			System.out.println("删除文件操作出错");
			e.printStackTrace();

		}

	}

	/**
	 * 删除文件夹
	 * 
	 * @param filePathAndName
	 *            String 文件夹路径及名称 如c:/fqf
	 * @param fileContent
	 *            String
	 * @return boolean
	 */
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			File myFilePath = new File(filePath);
			myFilePath.delete(); // 删除空文件夹

		} catch (Exception e) {
			System.out.println("删除文件夹操作出错");
			e.printStackTrace();

		}

	}

	/**
	 * 删除文件夹里面的所有文件
	 * 
	 * @param path
	 *            String 文件夹路径 如 c:/fqf
	 */
	public static void delAllFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);// 再删除空文件夹
			}
		}
	}

	/**
	 * 复制整个文件夹内容
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf/ff
	 * @return boolean
	 */
	public static void copyFolder(String oldPath, String newPath) {

		try {
			(new File(newPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + file[i]);
				} else {
					temp = new File(oldPath + File.separator + file[i]);
				}

				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath + "/" + (temp.getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// 如果是子文件夹
					copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
				}
			}
		} catch (Exception e) {
			System.out.println("复制整个文件夹内容操作出错");
			e.printStackTrace();

		}

	}

	/**
	 * 复制单个文件
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf.txt
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public static void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { // 文件存在时
				InputStream inStream = new FileInputStream(oldPath); // 读入原文件
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		} catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();

		}

	}

	/**
	 * 移动文件到指定目录
	 * 
	 * @param oldPath
	 *            String 如：c:/fqf.txt
	 * @param newPath
	 *            String 如：d:/fqf.txt
	 */
	public static void moveFile(String oldPath, String newPath) {
		copyFile(oldPath, newPath);
		delFile(oldPath);

	}

	/**
	 * 移动文件到指定目录
	 * 
	 * @param oldPath
	 *            String 如：c:/fqf.txt
	 * @param newPath
	 *            String 如：d:/fqf.txt
	 */
	public static void moveFolder(String oldPath, String newPath) {
		copyFolder(oldPath, newPath);
		delFolder(oldPath);

	}

	// 拷贝文件
	private static void copyFile2(String source, String dest) {
		try {
			File in = new File(source);
			File out = new File(dest);
			FileInputStream inFile = new FileInputStream(in);
			FileOutputStream outFile = new FileOutputStream(out);
			byte[] buffer = new byte[10240];
			int i = 0;
			while ((i = inFile.read(buffer)) != -1) {
				outFile.write(buffer, 0, i);
			} // end while
			inFile.close();
			outFile.close();
		} // end try
		catch (Exception e) {

		} // end catch
	}// end copyFile

	public static String getPrintSize(String file) {
		DecimalFormat df = new DecimalFormat("#.00");
		long fileS = Long.valueOf(file);
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	private final static Pattern PATTERN = Pattern.compile("\\((\\d+)\\)");

	public static Map<String, String> getFileNameAdd(String name,Boolean reName) {
		
		if (name == null) {
			return null;
		}

		/*Matcher matcher = PATTERN.matcher(name);
		if (matcher.find()) {
			//int no = Integer.parseInt(matcher.group(1)) + 1;
			return name.replaceAll(PATTERN.toString(), "(".concat(String.valueOf(DateUtils.currtimeTolong19())).concat(")"));
		}*/
		
		int endIndex = name.lastIndexOf(".");
		String head = name.substring(0, endIndex);
		String foot = name.substring(endIndex+1, name.length());
		
		Map<String, String> map = new HashMap<>();
		map.put("head",head);
		map.put("foot",foot);
		if(reName){
			map.put("name",head.concat("("+DateUtils.currtimeTolong19()+").").concat(foot));
		}else{
			map.put("name",name);
		}
		return map;
	}

	public static boolean fileToZip(String sourceFilePath,String zipFilePath,String fileName){  
        boolean flag = false;  
        File sourceFile = new File(sourceFilePath);  
        FileInputStream fis = null;  
        BufferedInputStream bis = null;  
        FileOutputStream fos = null;  
        ZipOutputStream zos = null;  
          
        if(sourceFile.exists() == false){  
            System.out.println("待压缩的文件目录："+sourceFilePath+"不存在.");  
        }else{  
            try {  
                File zipFile = new File(zipFilePath + "/" + fileName +".zip");  
                if(zipFile.exists()){  
                    System.out.println(zipFilePath + "目录下存在名字为:" + fileName +".zip" +"打包文件.");  
                }else{  
                    File[] sourceFiles = sourceFile.listFiles();  
                    
                    if(null == sourceFiles || sourceFiles.length<1){  
                        System.out.println("待压缩的文件目录：" + sourceFilePath + "里面不存在文件，无需压缩."); 
                    }else{  
                    	 fos = new FileOutputStream(zipFile);  
                         zos = new ZipOutputStream(new BufferedOutputStream(fos)); 
                        byte[] bufs = new byte[1024*10];  
                        for(int i=0;i<sourceFiles.length;i++){  
                        	File srcFile = sourceFiles[i];
                        	if (srcFile.isDirectory()) {
                        		continue;
                        	}
                            //创建ZIP实体，并添加进压缩包  
                            ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());  
                            zos.putNextEntry(zipEntry);  
                            //读取待压缩的文件并写进压缩包里  
                            fis = new FileInputStream(sourceFiles[i]);  
                            bis = new BufferedInputStream(fis, 1024*10);  
                            int read = 0;  
                            while((read=bis.read(bufs, 0, 1024*10)) != -1){  
                                zos.write(bufs,0,read);  
                            }  
                        }  
                        flag = true;  
                    }  
                }  
            } catch (FileNotFoundException e) {  
                e.printStackTrace();  
                //throw new RuntimeException(e);  
            } catch (IOException e) {  
                e.printStackTrace();  
               // throw new RuntimeException(e);  
            } finally{  
                //关闭流  
                try {  
                    if(null != bis) bis.close();  
                    if(null != zos) zos.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                    throw new RuntimeException(e);  
                }  
            }  
        }  
        return flag;  
    }  
	
	
	/**
	 * 
	 * @author: xiaoz
	 * @2017年7月6日 
	 * @功能描述:
	 * @param files 文件
	 * @param dir 保存文件夹
	 * @param httpPath 访问路径
	 * @return
	 */
	public static List<String> fileUpload(MultipartFile[] files,String dir,String httpPath){
		
		if(files == null){
			
			return null;
		}	
		
		List<String> fileList = new ArrayList<>();
		
		
		for(MultipartFile file : files){
			  
			  String fileName = file.getOriginalFilename();//获取上传的文件名字 日后可以根据文件名做相应的需改 例如自定义文件名 分析文件后缀名等等  
			  
			  Date date = new Date();
			  
			  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			  
			  String dateString = simpleDateFormat.format(date);
			  
			  String newFileName = dateString+"_"+fileName;
			  
			  File dirr = new File(dir);
			  
			  File targetFile = new File(dir , newFileName);  //新建文件  
			  
			  try {
				  
				  String os = System.getProperty("os.name"); 
				  
					if (!dirr.exists()) {
						  
						dirr.mkdirs();  //如果文件不存在  在目录中创建文件夹   这里要注意mkdir()和mkdirs()的区别  
						
						if(!os.toLowerCase().startsWith("Win")){
						 
							Runtime.getRuntime().exec("chmod 777 " + dirr.getPath()); 
						}  
						
					}  
				  
				    file.transferTo(targetFile);
				    
				    targetFile.setExecutable(true);//设置可执行权限
				    targetFile.setReadable(true);//设置可读权限
					targetFile.setWritable(true);//设置可写权限
					
					String saveFilename = targetFile.getPath();
					
					if(!os.toLowerCase().startsWith("win")){  
						 
						Runtime.getRuntime().exec("chmod 777 " + saveFilename); 
					} 
				    
				    String newPath = httpPath+"/"+newFileName;
				    
				    fileList.add(newPath);
				    
			  } catch (Exception e) {
				  
				e.printStackTrace();  
				
			  }  
		}
		
		return fileList;
		
	}

//	/**
//	 * 采用指定宽度、高度或压缩比例 的方式对图片进行压缩
//	 * @param imgsrc 源图片地址
//	 * @param imgdist 目标图片地址
//	 * @param widthdist 压缩后图片宽度（当rate==null时，必传）
//	 * @param heightdist 压缩后图片高度（当rate==null时，必传）
//	 * @param rate 压缩比例
//	 */
//	public static void reduceImg(String imgsrc, String imgdist, int widthdist,
//								 int heightdist, Float rate) {
//		try {
//			File srcfile = new File(imgsrc);
//			// 检查文件是否存在
//			if (!srcfile.exists()) {
//				return;
//			}
//			// 如果rate不为空说明是按比例压缩
//			if (rate != null && rate > 0) {
//				// 获取文件高度和宽度
//				int[] results = getImgWidth(srcfile);
//				if (results == null || results[0] == 0 || results[1] == 0) {
//					return;
//				} else {
//					widthdist = (int) (results[0] * rate);
//					heightdist = (int) (results[1] * rate);
//				}
//			}
//			// 开始读取文件并进行压缩
//			Image src = javax.imageio.ImageIO.read(srcfile);
//			BufferedImage tag = new BufferedImage((int) widthdist,
//					(int) heightdist, BufferedImage.TYPE_INT_RGB);
//
//			tag.getGraphics().drawImage(
//					src.getScaledInstance(widthdist, heightdist,
//							Image.SCALE_SMOOTH), 0, 0, null);
//
//			FileOutputStream out = new FileOutputStream(imgdist);
//			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
//			encoder.encode(tag);
//			out.close();
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}


//	/**
//	 * 获取图片宽度
//	 *
//	 * @param file
//	 *            图片文件
//	 * @return 宽度、高度
//	 */
//	public static int[] getImgWidth(File file) {
//		InputStream is = null;
//		BufferedImage src = null;
//		int result[] = { 0, 0 };
//		try {
//			is = new FileInputStream(file);
//			src = javax.imageio.ImageIO.read(is);
//			result[0] = src.getWidth(null); // 得到源图宽
//			result[1] = src.getHeight(null); // 得到源图高
//			is.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return result;
//	}


	/**
	 * @param titleMap 表头
	 * @param list 数据
	 * @param uploadPath 上传路径 如：PropertiesUtil.FILE_UPLOAD_PATH+userId+"/客户订房信息.xlsx"
	 * @return
	 */
	public static  boolean writeExcel(LinkedHashMap<String, String> titleMap,List<Map<String, Object>> list,String uploadPath) {

		if(StringUtils.isBlank(uploadPath) || titleMap == null ||titleMap.isEmpty()){
			return false;
		}

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = null;
		XSSFCellStyle style_title = wb.createCellStyle();
		Font fontHeader=wb.createFont();
		fontHeader.setFontHeightInPoints((short)12);
		fontHeader.setBold(true);
		//字体名称
		fontHeader.setFontName("宋体");
		style_title.setFont(fontHeader);
		style_title.setAlignment(XSSFCellStyle.ALIGN_CENTER);
		style_title.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);

		XSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(XSSFCellStyle.ALIGN_CENTER);
		style_title.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);


		List<Map<String, Object>> temptList = null;

		int ss = list.size() / FileUtil.MAX_EXCELL_ROWS;

		for(int j = 0;j<=ss;j++){

			temptList = list.subList((FileUtil.MAX_EXCELL_ROWS)*j,(FileUtil.MAX_EXCELL_ROWS)*(j+1)>list.size()?list.size():(FileUtil.MAX_EXCELL_ROWS)*(j+1));

			sheet = wb.createSheet(j+"");
			XSSFRow row = sheet.createRow((int) 0);
			row.setHeightInPoints(30);
			XSSFCell cell = null;
			int cell_0 = 0;
			for (String key : titleMap.keySet()) {
				cell = row.createCell(cell_0);
				cell.setCellValue(titleMap.get(key));
				cell.setCellStyle(style_title);
				cell_0 ++;
			}
			if(temptList != null && temptList.size()>0){
				for (int i = 0; i < temptList.size(); i++) {
					row = sheet.createRow(i+1);
					cell_0 = 0;

					Map<String,Object> rowMap = temptList.get(i);
					for (String key : titleMap.keySet()) {
						cell = row.createCell(cell_0);
						if(key.equals("index")){
							cell.setCellValue((i+1));
						}else{
							Object value = rowMap.get(key);
							if(null == value){
								cell.setCellValue("--");
							}else{
								cell.setCellValue(value+"");
							}
						}
						cell.setCellStyle(style);
						cell_0 ++;
					}
				}
			}
		}

		FileOutputStream outputStream = null;
		try {
			// //给文件夹设置读写修改操作
			String os = System.getProperty("os.name");
			outputStream = new FileOutputStream(uploadPath);
			wb.write(outputStream);
			wb.close();
			outputStream.close();

			//给文件设置读写修改操作
			File targetFile = new File(uploadPath);

			if(targetFile.exists()){
				targetFile.setExecutable(true);//设置可执行权限
				targetFile.setReadable(true);//设置可读权限
				targetFile.setWritable(true);//设置可写权限
				String saveFilename = targetFile.getPath();
				if(!os.toLowerCase().startsWith("win")){
					Runtime.getRuntime().exec("chmod 777 " + saveFilename);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}


      
    public static void main2(String[] args){
    	
//    		//F:\wendanguanli\download\20170921142519\aa\aaaaaaaaaaaaaaaaaaa
//        boolean flag = fileToZip("F:\\wendanguanli\\download\\20170921142519\\aa\\aaaaaaaaaaaaaaaaaaa", "F://", "新建文件夹 (3)");
//        if(flag){
//            System.out.println("文件打包成功!");
//        }else{
//            System.out.println("文件打包失败!");
//        }

		LinkedHashMap<String, String> tilleMap = new LinkedHashMap<String, String>();
		tilleMap.put("index","序号");
		tilleMap.put("sz","数字");

		int listSize = 80000;

		long start = System.currentTimeMillis();
		//定长比不定长速度快了一倍  28 ： 49
		List<Map<String,Object>> maps = new ArrayList<>(listSize);
		for(int i = 1;i < listSize;i++){
			Map<String,Object> map = new HashMap<>();
			map.put("sz",i);
			maps.add(map);
		}

		long end = System.currentTimeMillis();

		out.print(end - start);

		//调用公共Excell导出接口
		String path =PropertiesUtil.FILE_UPLOAD_PATH+"761"+"/客户订房信息.xlsx";
		String httpPath = PropertiesUtil.FILE_HTTP_PATH+"761"+"/客户订房信息.xlsx";

		FileUtil.writeExcel(tilleMap,maps,path);

		maps = null;

		System.out.print("输入完成");




		/*
		 * 图片压缩
		 */
		/*System.out.println("压缩图片开始...");
		String imagesrc = "F://imageC//P5280006.JPG";
		String imgdist = imagesrc+"_"+DateUtils.dateToString(new Date(),"yyyy_MM_dd_HH_mm_ss")+imagesrc.substring(imagesrc.lastIndexOf("."),imagesrc.length());
		File srcfile = new File(imagesrc);
		System.out.println("压缩前srcfile size:" + srcfile.length());
		System.out.println("压缩前srcfile size:" + srcfile.getPath());
		System.out.println("压缩前srcfile size:" + srcfile.getName());

		int[] imageProperty = getImgWidth(srcfile);

		DateUtils.dateToString(new Date(),"yyyy-MM-dd :HH:mm:ss");
		reduceImg(imagesrc, imgdist,
				(imageProperty == null || imageProperty.length == 0)?1000:imageProperty[0], (imageProperty == null || imageProperty.length == 0)?1000:imageProperty[1],null);
		File distfile = new File(imgdist);
		System.out.println("压缩后distfile size:" + distfile.length());*/

    }  

}
