package hq.myhome.controller.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;

import hq.mydb.dao.BaseDAO;
import hq.myhome.utils.MyHomeHelper;

/**
 * 一些前后台公用的借口
 * @author Administrator
 *
 */

@Controller // 声明当前为Controller
@RequestMapping(value = "/utils") // 制定基础URL
public class UtilsController {
	private BaseDAO baseDAO;

	@Autowired
	public UtilsController(BaseDAO baseDAO) {
		this.baseDAO = baseDAO;
	}

	/**
	 * 注册页面
	 * @return
	 */
	@RequestMapping(value = "/uploadImg", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject uploadImg(@RequestParam(value = "file") MultipartFile pic, HttpServletRequest request) {
		int code = 0;// 上传图片返回码,0代表成功,其它代表失败
		JSONObject joImg = new JSONObject();
		if (!pic.isEmpty()) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String basePath = "resources" + File.separator + "uploadImg" + File.separator + sdf.format(new Date()) + File.separator;
			String diskPath = request.getSession().getServletContext().getRealPath("/") + basePath;
			String originalFileName = pic.getOriginalFilename();
			// 新的图片名称
			String newFileName = MyHomeHelper.createUUID() + originalFileName.substring(originalFileName.lastIndexOf("."));
			// 新的图片
			File newFile = new File(diskPath + newFileName);
			if (!newFile.exists()) {
				newFile.mkdirs();
			}
			// 将内存中的数据写入磁盘
			try {
				pic.transferTo(newFile);
				String src = request.getContextPath() + "/" + basePath + newFileName;
				joImg.put("src", src.replace("\\", "/"));
			} catch (IllegalStateException e) {
				code = -1;
			} catch (IOException e) {
				code = -1;
			}
		}

		JSONObject returnJSONObject = new JSONObject();
		returnJSONObject.put("code", code);
		returnJSONObject.put("data", joImg);

		return returnJSONObject;
	}
}
