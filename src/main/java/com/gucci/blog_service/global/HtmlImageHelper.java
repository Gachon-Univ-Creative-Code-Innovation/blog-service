package com.gucci.blog_service.global;
import com.gucci.blog_service.post.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HtmlImageHelper {

    private final S3Service s3Service;

    /** HTML 내 img 태그의 src(objectKey)를 presigned URL로 치환 */
    public String convertImageKeysToPresignedUrls(String html) {
        Document doc = Jsoup.parseBodyFragment(html);
        Elements imgTags = doc.select("img");

        for (Element img : imgTags) {
            String key = img.attr("src"); // objectKey
            if (key != null && !key.startsWith("http")) {
                String presignedUrl = s3Service.getPresignedUrl(key);
                img.attr("src", presignedUrl);
            }
        }

        return doc.body().html();
    }

    /** 저장된 게시글에서 첫번째 이미지 objectkey값 반환 */
    public String extractFirstImageFromSavedContent(String html) {
        Document doc = Jsoup.parseBodyFragment(html);
        Elements imgTags = doc.select("img");

        if (!imgTags.isEmpty()) {
            // 첫 번째 이미지의 src 값을 반환
            return imgTags.get(0).attr("src");
        }

        return null; // 이미지가 없으면 null 반환
    }


    /**
     * HTML에서 <img> 태그의 src를 objectKey로 정제
     * <img src="https://objectstorage.../abc123.jpg?...presigned" />
     * to
     * <img src="uploads/abc123.jpg" />
     */
    public String extractObjectKeysFromPresignedUrls(String html) {
        Document doc = Jsoup.parseBodyFragment(html);
        Elements imgTags = doc.select("img");

        for (Element img : imgTags) {
            String src = img.attr("src");
            if (src.startsWith("http")) {
                String objectKey = extractKeyFromUrl(src);
                img.attr("src", objectKey);
            }
        }

        return doc.body().html();
    }


    /**
     * presigned URL에서 object key 추출
     * 예: https://alog-profile-images.../post/...fileName?... -> post/...fileName
     */
    private String extractKeyFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath(); // "https://alog-profile-images.../post/...fileName?..."
            int objectStart = path.indexOf("/post/");
            if (objectStart != -1) {
                return path.substring(objectStart + 1); // "post/...fileName"
            }
            return urlString; // fallback
        } catch (Exception e) {
            return urlString; // fallback
        }
    }


    /**
     * 저장된 postDoc.content()에서 img src에 있는 이미지의 objectKey들만 추출
     */
    public List<String> extractObjectKeysFromSavedContent(String savedContent) {
        List<String> objectKeys = new ArrayList<>();
        Document doc = Jsoup.parseBodyFragment(savedContent);
        Elements imgTags = doc.select("img");

        for (Element img : imgTags) {
            String src = img.attr("src");
            if (!src.startsWith("http")) {
                objectKeys.add(src); // src = "uploads/abc123.jpg"
            }
        }

        return objectKeys;
    }

}

