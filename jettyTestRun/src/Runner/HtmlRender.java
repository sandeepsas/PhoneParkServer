/*package Runner;

public class HtmlRender {
	

	public static String render(String file, Map<String, Object> locals) {
		return layout(file, parseFile(file, "\\$\\{(\\w.*?)\\}", locals));
	}

	public static String layout(String file, String content) {
		HashMap<String, Object> layout = new HashMap<String, Object>();
		layout.put("content", content);
		return parseFile(file, "@\\{(content)\\}", layout);
	}

	public static String parse(String pattern, String text, Map<String, Object> locals) {
		Matcher regexp = Pattern.compile(pattern).matcher(text);
		while (regexp.find()) {
			text = regexp.replaceFirst(locals.get(regexp.group(1)).toString());
		}
		return text;
	}

	@SuppressWarnings("finally")
	public static String parseFile(String file, String pattern, Map<String, Object> locals) {
		StringBuffer content = new StringBuffer("");
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(file));
			String line = null;

			while ((line = buffer.readLine()) != null) {
				content.append(parse(pattern, line, locals) + "\n");
			}

			buffer.close();
		}
		catch (Exception exception) {
			System.out.printf("ERROR: %s\n", exception.getMessage());
		}
		finally {
			return content.toString();
		}
	}
	
	

		Spark.get("/home",  new Route() {

			
			@Override
			public Object handle(Request request, Response response) {
		        URL location = StartServer.class.getProtectionDomain().getCodeSource().getLocation();
		        System.out.println(location.getFile());
				return render("E:\\Program_Workspaces\\Server Programming\\GitVersion\\PhoneParkServer\\jettyTestRun\\WebContent\\index.html",null);
			}
		});

}
*/