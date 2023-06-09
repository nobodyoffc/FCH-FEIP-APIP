package FreeGetAPIs;

import APIP17V1_Avatar.AvatarMaker;
import initial.Initiator;
import startAPIP.RedisKeys;

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

import static api.Constant.*;

@WebServlet(FreeGet + GetAvatarAPI)
public class GetAvatar extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (!Initiator.isFreeGetAllowed(response.getWriter())) return;

        String fidRequested = request.getParameter("fid");

        if(!(fidRequested.substring(0,1).equals("F") || fidRequested.substring(0,1).equals("3")))return;

        String avatarBasePath = Initiator.jedis0Common.get(RedisKeys.AvatarBasePath);
        String avatarPngPath = Initiator.jedis0Common.get(RedisKeys.AvatarPngPath);
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