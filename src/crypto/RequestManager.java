package crypto;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.visualizers.RequestViewHTTP;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class RequestManager {
    private String[] str = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
    private StringBuilder urlSB;
    private String domain = null;
    private String path = null;
    private String sign = null;
    private String nonce = null;
    private String timestamp = null;
    private String body = "";
    private TreeMap<String,String> params = null;
    private HTTPSamplerProxy sampler = null;
    private String secretKey = "";


    public RequestManager(String url,HTTPSamplerProxy sampler) {
        this.urlSB = new StringBuilder(url);
        this.sampler = sampler;
        timestamp = String.valueOf(System.currentTimeMillis());
        init();
        genSign();
    }

    public RequestManager(String url,String key,HTTPSamplerProxy sampler) {
        this.urlSB = new StringBuilder(url);
        this.secretKey = key;
        this.sampler = sampler;
        timestamp = String.valueOf(System.currentTimeMillis());
        init();
        genSign();
    }

    private void initDomain(){
        String prefix = "https://";
        int prefixIndex = urlSB.indexOf(prefix);
        if(urlSB.indexOf(prefix)!= -1)
            urlSB.delete(prefixIndex,prefixIndex+prefix.length());
        domain = urlSB.substring(0,urlSB.indexOf("/"));
        int domainIndex = urlSB.indexOf(domain);
        urlSB.delete(domainIndex,domainIndex+domain.length());
    }

    private void initPath(){
        path = urlSB.substring(0,urlSB.indexOf("?")+1);
        int pathIndex = urlSB.indexOf(path);
        urlSB.delete(pathIndex,pathIndex+path.length());
    }

    private void mapQueryToParams(){
        Map<String, String[]> queryMap = RequestViewHTTP.getQueryMap(urlSB.toString());
        Set<String> strings = queryMap.keySet();
        for (String string : strings) {
            if(!(string.equals("nonce")||string.equals("sign")))
                params.put(string,queryMap.get(string)[0]);
        }
    }

    private void initBody(){
        Arguments args = sampler.getArguments();
        if(args.getArgumentCount()!=0){
            Argument arg_body = args.getArgument(0);
            body = arg_body.getValue();
        }
    }

    private void setNonce(){
        StringBuilder nonce = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            int random = (int) (Math.random() * str.length);
            nonce.append(str[random]);
        }
        this.nonce = nonce.toString();
    }

    private void genSign(){
        if(secretKey.isEmpty())
            secretKey = Config.getSecretKey(domain);

        MD5 md5 = new MD5(secretKey,nonce,params,timestamp,body);
        sign = md5.genSign();
        setPath(path+md5.getFixedPath());
    }

    private void init(){
        params = new TreeMap<>();
        initDomain();
        initPath();
        mapQueryToParams();
        initBody();
        setNonce();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDomain() {
        return domain;
    }

    public String getNonce() {
        return nonce;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSign() {
        return sign;
    }

    public String getBody() {
        return body;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
