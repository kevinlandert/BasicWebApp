package com.develogical;

import com.develogical.web.ApiResponse;
import com.develogical.web.IndexPage;
import com.develogical.web.ResultsPage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class WebServer {

  public WebServer() throws Exception {

    Server server = new Server(portNumberToUse());

    ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping(new ServletHolder(new Website()), "/*");
    handler.addServletWithMapping(new ServletHolder(new Api()), "/api/*");
    server.setHandler(handler);

    server.start();
  }

  static class Website extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      String query = req.getParameter("q");
      if (query == null) {
        new IndexPage().writeTo(resp);
      } else {
        new ResultsPage(query, new QueryProcessor().process(query)).writeTo(resp);
      }
    }
  }

  static class Api extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      String query = req.getParameter("q");
      if (query.toLowerCase().contains("donkluser")) {
    	  ServletContext cntx = req.getServletContext();
    	  String filename = cntx.getRealPath("Images/donkluser.png");
    	  String mime = cntx.getMimeType(filename);
    	  if (mime == null) {
        	  resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        	  return;  
    	  }
          resp.setContentType(mime);
          File file = new File(filename);
          resp.setContentLength((int)file.length());
          
          FileInputStream in = new FileInputStream(file);
          OutputStream out = resp.getOutputStream();
          
          byte[] buf = new byte[1024];
          int count = 0;
          while ((count = in.read(buf)) >= 0) {
        	  out.write(buf,0,count);
          }
          out.close();
          in.close();
      }
      else {
          new ApiResponse(new QueryProcessor().process(query)).writeTo(resp);

      }

      
    }
  }

  private Integer portNumberToUse() {
    return System.getenv("PORT") != null ? Integer.valueOf(System.getenv("PORT")) : 8080;
  }

  public static void main(String[] args) throws Exception {
    new WebServer();
  }

}