package com.medicalchain.block;

import com.alibaba.fastjson.JSON;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.java_websocket.WebSocket;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.EnumSet;


public class HTTPService {
	
    private BlockService blockService;
    private P2PService   p2pService;

    public HTTPService(BlockService blockService, P2PService p2pService) {
        this.blockService = blockService;
        this.p2pService = p2pService;
    }

    public void initHTTPServer(int port) {
        try {
            Server server = new Server(port);
            System.out.println("listening http port on: " + port);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            
  
            FilterHolder filter = new FilterHolder();
            filter.setInitParameter("allowedOrigins", "*");
            filter.setInitParameter("allowedMethods", "POST,GET,OPTIONS,PUT,DELETE,HEAD");
            filter.setInitParameter("allowedHeaders", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
            filter.setInitParameter("preflightMaxAge", "728000");
            filter.setInitParameter("allowCredentials", "true");
            CrossOriginFilter corsFilter = new CrossOriginFilter();
            filter.setFilter(corsFilter);
            context.addFilter(filter, "/*", EnumSet.of(DispatcherType.REQUEST));
            
            
            server.setHandler(context);
            context.addServlet(new ServletHolder(new BlocksServlet()), "/blocks");
            context.addServlet(new ServletHolder(new GetBlockCountServlet()), "/getBlockCount");
            context.addServlet(new ServletHolder(new GetTranCountServlet()), "/getTranCount");
            context.addServlet(new ServletHolder(new AddBlockServlet()), "/addBlock");
            context.addServlet(new ServletHolder(new PeersServlet()), "/peers");
            context.addServlet(new ServletHolder(new AddPeerServlet()), "/addPeer");
            server.start();
            server.join();
        } catch (Exception e) {
            System.out.println("init http server is error:" + e.getMessage());
        }
    }

    private class BlocksServlet extends HttpServlet {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setCharacterEncoding("UTF-8");
			String countString = req.getParameter("count");
			if(countString != null) {
				int count = Integer.valueOf(countString);
				resp.getWriter().println(JSON.toJSONString(blockService.getBlockChain(count)));
			} else {
				resp.getWriter().println(JSON.toJSONString(blockService.getBlockChain()));
			}
          
 
        }
    }
    
    private class GetBlockCountServlet extends HttpServlet {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().println(blockService.getBlockChain().size());
        }
    }

    
    private class GetTranCountServlet extends HttpServlet {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");
            long count = 0;
            for(Block block: blockService.getBlockChain()) {
            	count = count + block.getData().getCount();
            }
            resp.getWriter().println(count);
        }
    }


    private class AddPeerServlet extends HttpServlet {
        /**
		 * 
		 */
		private static final long serialVersionUID = -1081879765433167836L;

		@Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            this.doPost(req, resp);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");
            String peer = req.getParameter("peer");
            p2pService.connectToPeer(peer);
            resp.getWriter().print("ok");
        }
    }


    private class PeersServlet extends HttpServlet {
        /**
		 * 
		 */
		private static final long serialVersionUID = -8243681853562509948L;

		@Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");
            for (WebSocket socket : p2pService.getSockets()) {
                InetSocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
                resp.getWriter().print(remoteSocketAddress.getHostName() + ":" + remoteSocketAddress.getPort());
                resp.getWriter().print("\n");
            }
        }
    }


    private class AddBlockServlet extends HttpServlet {
        /**
		 * 
		 */
		private static final long serialVersionUID = -8171550161672949387L;

		@Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            this.doPost(req, resp);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");
            String user = req.getParameter("user");
            String type = req.getParameter("type");
            String count = req.getParameter("count");
            DataVo vo = new DataVo();
            vo.setCount(Integer.valueOf(count));
            vo.setType(type);
            vo.setUser(user);
            Block newBlock = blockService.generateNextBlock(vo);
            blockService.addBlock(newBlock);
            p2pService.broatcast(p2pService.responseLatestMsg());
            String s = JSON.toJSONString(newBlock);
            System.out.println("block added: " + s);
            resp.getWriter().print(s);
        }
    }
}

