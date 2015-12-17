/**
 * RandromKeyStore.java Created on 2015-11-12
 */
package cn.dotui.ant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * The class <code>RandromKeyStore</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class RandromKeyStore extends Task {

	private String path;

	private String properties;

	private String CMD = "keytool -genkey -v -keystore %s -alias cn.dotui -keyalg RSA -keysize 2048 -validity 100000 -keypass cn.dotui -storepass cn.dotui -dname \"CN=CN, OU=OU, O=OU, L=CN, ST=GD, C=CN\"";

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */
	@Override
	public void execute() throws BuildException {
		if (this.path == null || checkOutput(path)) {
			throw new BuildException("请检查 path是否存在");
		}

//		if (this.properties == null || checkOutput(properties)) {
//			throw new BuildException("请检查 properties是否存在");
//		}

		genKeyStore();

//		addOrReplaceProperties();
		super.execute();
	}

	private boolean addOrReplaceProperties() throws BuildException {

		final Properties prop = new Properties();
		try {
			System.out.println("读取" + properties + "文件");
			prop.load(new FileInputStream(properties));

			System.out.println("修改" + properties + "文件值");
			prop.setProperty("key.store", path.replace("\\", "\\\\"));
			prop.setProperty("key.alias", "cn.dotui");
			prop.setProperty("key.store.password", "cn.dotui");
			prop.setProperty("key.alias.password", "cn.dotui");

			final BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(properties)));
			Object[] objs = prop.keySet().toArray();
			for (int i = 0; i < objs.length; i++) {
				System.out.println(String.format("key:%s value:%s", objs[i],
						prop.get(objs[i])));
				bw.write(objs[i] + "=" + prop.get(objs[i]));
				bw.newLine();
			}
			bw.close();
			System.out.println("修改完成");

		} catch (IOException e) {
			throw new BuildException("修改Properties错误");
		}

		return true;
	}

	private boolean genKeyStore() throws BuildException {
		try {
			String coding = System.getProperty("sun.jnu.encoding", "GBK");
			path = this.path + File.separator + "dotui.keystore";
			final String cmd = String.format(CMD, path);
			System.out.println("执行命令:" + cmd);
			Process exec = Runtime.getRuntime().exec(cmd);

			// exec.waitFor();
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(exec.getInputStream(), coding));
			String line = null;
			while (null != (line = reader.readLine())) {
				System.out.println(line);
			}
			if (exec.exitValue() == 0) {
				System.out.println(String.format("生成签名成功,保存在 [%s]", path));
				return true;
			} else {
				throw new BuildException("生成签名错误");
			}
		} catch (Exception e) {
			throw new BuildException("生成签名错误");
		}
	}

	/**
	 * 
	 * 
	 * @return
	 */
	private boolean checkOutput(String input) {
		if (input != null) {
			final File file = new File(input);
			if (file.exists()) {
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {
		RandromKeyStore keyStore = new RandromKeyStore();
		keyStore.setPath("D:\\workspace\\ant_random_keys");
		keyStore.setProperties("o.properties");
		keyStore.genKeyStore();
		keyStore.addOrReplaceProperties();
	}
}
