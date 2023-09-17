package FreeGetAPIs;

import avatar.AvatarMaker;
import constants.ApiNames;
import data.ReplierForFree;
import initial.Initiator;
import constants.Strings;
import redis.clients.jedis.Jedis;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import static constants.Strings.CONFIG;
import static initial.Initiator.jedisPool;


@WebServlet(ApiNames.FreeGet + ApiNames.GetAvatarAPI)
public class GetAvatar extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fidRequested;
        String avatarBasePath = null;
        String avatarPngPath = null;
        PrintWriter writer = response.getWriter();
        ReplierForFree replier = new ReplierForFree();

        if (Initiator.isFreeGetForbidden(writer)) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            replier.setOther();
            replier.setData("Error: FreeGet API is not active now.");
            writer.write(replier.toJson());
            return;
        }
        fidRequested = request.getParameter("fid");
        if (!(fidRequested.charAt(0) == 'F' || fidRequested.charAt(0) == '3')) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            replier.setOther();
            replier.setData("Error: It is not a FID.");
            writer.write(replier.toJson());
            return;
        }
        try(Jedis jedis0Common= jedisPool.getResource()) {
            avatarBasePath = jedis0Common.hget(CONFIG, Strings.AVATAR_BASE_PATH);
            avatarPngPath = jedis0Common.hget(CONFIG, Strings.AVATAR_PNG_PATH);
        }catch (Exception e){
             e.printStackTrace();
        }
        if(!avatarPngPath.endsWith("/"))avatarPngPath  = avatarPngPath+"/";
        if(!avatarBasePath.endsWith("/"))avatarBasePath = avatarBasePath+"/";

        AvatarMaker.getAvatars(new String[]{fidRequested},avatarBasePath,avatarPngPath);

        response.reset();
        response.setContentType("image/png");
        File file = new File(avatarPngPath+fidRequested+".png");
        BufferedImage buffImg = ImageIO.read(new FileInputStream(file));
        ServletOutputStream servletOutputStream = response.getOutputStream();
        ImageIO.write(buffImg, "png", servletOutputStream);
        servletOutputStream.close();
        file.delete();
    }
}