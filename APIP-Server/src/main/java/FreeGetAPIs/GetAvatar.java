package FreeGetAPIs;

import APIP17V1_Avatar.AvatarMaker;
import constants.ApiNames;
import initial.Initiator;
import constants.Strings;

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

@WebServlet(ApiNames.FreeGet + ApiNames.GetAvatarAPI)
public class GetAvatar extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (Initiator.isFreeGetForbidden(response.getWriter())) return;

        String fidRequested = request.getParameter("fid");

        if(!(fidRequested.substring(0,1).equals("F") || fidRequested.substring(0,1).equals("3")))return;

        String avatarBasePath = Initiator.jedis0Common.hget(CONFIG,Strings.AVATAR_BASE_PATH);
        String avatarPngPath = Initiator.jedis0Common.hget(CONFIG,Strings.AVATAR_PNG_PATH);
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