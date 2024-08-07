package APIP17V1_Avatar;

import avatar.AvatarMaker;
import constants.ApiNames;
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

import static constants.Strings.CONFIG;

@WebServlet(ApiNames.APIP17V1Path + ApiNames.GetAvatarAPI)
public class getAvatar extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String fidRequested = request.getParameter("fid");

        String avatarBasePath;
        String avatarPngPath;
        try(Jedis jedis = Initiator.jedisPool.getResource()) {
            avatarBasePath = jedis.hget(CONFIG, Strings.AVATAR_ELEMENTS_PATH);
            avatarPngPath = jedis.hget(CONFIG, Strings.AVATAR_PNG_PATH);
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