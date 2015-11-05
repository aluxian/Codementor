package com.aluxian.codementor.data.cookies;

import android.content.Context;
import android.content.SharedPreferences;

import com.aluxian.codementor.services.ErrorHandler;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @source https://gist.github.com/franmontiel/ed12a2295566b7076161
 */
public class PersistentCookieStore implements CookieStore {

    private static final String SP_NAME = "cookies";
    private static final String SP_KEY_DELIMITER = "|";
    private static final String SP_KEY_DELIMITER_REGEX = "\\" + SP_KEY_DELIMITER;

    private SharedPreferences sharedPreferences;
    private Map<URI, Set<HttpCookie>> allCookies;

    public PersistentCookieStore(Context context) {
        sharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        loadFromSharedPrefs();
    }

    @Override
    public synchronized void add(URI uri, HttpCookie cookie) {
        uri = getCookieUri(uri, cookie);
        Set<HttpCookie> targetCookies = allCookies.get(uri);

        if (targetCookies == null) {
            targetCookies = new HashSet<>();
            allCookies.put(uri, targetCookies);
        }

        // Replace if it already exists
        targetCookies.remove(cookie);
        targetCookies.add(cookie);

        saveToSharedPrefs(uri, cookie);
    }

    @Override
    public synchronized List<HttpCookie> get(URI uri) {
        return getValidCookies(uri);
    }

    @Override
    public synchronized List<HttpCookie> getCookies() {
        List<HttpCookie> allValidCookies = new ArrayList<>();

        for (URI uri : allCookies.keySet()) {
            allValidCookies.addAll(getValidCookies(uri));
        }

        return allValidCookies;
    }

    @Override
    public synchronized List<URI> getURIs() {
        return new ArrayList<>(allCookies.keySet());
    }

    @Override
    public synchronized boolean remove(URI uri, HttpCookie cookie) {
        Set<HttpCookie> targetCookies = allCookies.get(uri);

        if (targetCookies != null && targetCookies.remove(cookie)) {
            removeFromSharedPrefs(uri, cookie);
            return true;
        }

        return false;
    }

    @Override
    public synchronized boolean removeAll() {
        allCookies.clear();
        removeAllFromSharedPrefs();
        return true;
    }

    @SuppressWarnings("Convert2streamapi")
    private List<HttpCookie> getValidCookies(URI uri) {
        Set<HttpCookie> targetCookies = new HashSet<>();

        // If the stored URI does not have a path then it must match any URI in the same domain
        for (URI storedUri : allCookies.keySet()) {
            // Check if the domains match according to RFC 6265
            if (checkDomainsMatch(storedUri.getHost(), uri.getHost())) {
                // Check if the paths match according to RFC 6265
                if (checkPathsMatch(storedUri.getPath(), uri.getPath())) {
                    targetCookies.addAll(allCookies.get(storedUri));
                }
            }
        }

        // Check if there are expired cookies and remove them
        List<HttpCookie> cookiesToRemove = new ArrayList<>();
        for (Iterator<HttpCookie> it = targetCookies.iterator(); it.hasNext(); ) {
            HttpCookie currentCookie = it.next();
            if (currentCookie.hasExpired()) {
                cookiesToRemove.add(currentCookie);
                it.remove();
            }
        }

        if (!cookiesToRemove.isEmpty()) {
            removeFromSharedPrefs(uri, cookiesToRemove);
        }

        return new ArrayList<>(targetCookies);
    }

    /**
     * Get the real URI from the cookie's "domain" and "path" attributes.
     * If they are not set, use the URI provided (coming from the response).
     */
    private static URI getCookieUri(URI uri, HttpCookie cookie) {
        if (cookie.getDomain() != null) {
            // Remove the starting dot character of the domain, if it exists
            String domain = cookie.getDomain();
            if (domain.charAt(0) == '.') {
                domain = domain.substring(1);
            }

            try {
                String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
                String path = cookie.getPath() == null ? "/" : cookie.getPath();
                return new URI(scheme, domain, path, null);
            } catch (URISyntaxException e) {
                ErrorHandler.logWarn(e);
            }
        }

        return uri;
    }

    private String getSharedPrefsKey(URI uri, HttpCookie cookie) {
        return uri.toString() + SP_KEY_DELIMITER + cookie.getName();
    }

    private void loadFromSharedPrefs() {
        allCookies = new HashMap<>();
        Map<String, ?> allPairs = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : allPairs.entrySet()) {
            String[] uriAndName = entry.getKey().split(SP_KEY_DELIMITER_REGEX, 2);

            try {
                URI uri = new URI(uriAndName[0]);
                String encodedCookie = (String) entry.getValue();
                HttpCookie cookie = new SerializableHttpCookie().decode(encodedCookie);
                Set<HttpCookie> targetCookies = allCookies.get(uri);

                if (targetCookies == null) {
                    targetCookies = new HashSet<>();
                    allCookies.put(uri, targetCookies);
                }

                targetCookies.add(cookie);
            } catch (URISyntaxException e) {
                ErrorHandler.logWarn(e);
            }
        }
    }

    private void saveToSharedPrefs(URI uri, HttpCookie cookie) {
        String key = getSharedPrefsKey(uri, cookie);
        String value = new SerializableHttpCookie().encode(cookie);
        sharedPreferences.edit().putString(key, value).apply();
    }

    private void removeFromSharedPrefs(URI uri, List<HttpCookie> cookiesToRemove) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (HttpCookie cookieToRemove : cookiesToRemove) {
            editor.remove(getSharedPrefsKey(uri, cookieToRemove));
        }

        editor.apply();
    }

    private void removeFromSharedPrefs(URI uri, HttpCookie cookieToRemove) {
        sharedPreferences.edit().remove(getSharedPrefsKey(uri, cookieToRemove)).apply();
    }

    private void removeAllFromSharedPrefs() {
        sharedPreferences.edit().clear().apply();
    }

    /**
     * http://tools.ietf.org/html/rfc6265#section-5.1.3
     * <p>
     * A string domain-matches a given domain string if:
     * <p>
     * The domain string and the string are identical. (Note that both the domain string and the string will have
     * been canonicalized to lower case at this point)
     * <p>
     * Or, all of the following conditions hold:
     * - The domain string is a suffix of the string.
     * - The last character of the string that is not included in the domain string is a %x2E (".") character.
     * - The string is a host name (i.e., not an IP address).
     */
    private boolean checkDomainsMatch(String cookieHost, String requestHost) {
        return requestHost.equals(cookieHost) || requestHost.endsWith("." + cookieHost);
    }

    /**
     * http://tools.ietf.org/html/rfc6265#section-5.1.4
     * <p>
     * A request-path path-matches a given cookie-path if at least one of the following conditions holds:
     * <p>
     * - The cookie-path and the request-path are identical.
     * - The cookie-path is a prefix of the request-path, and the last character of the cookie-path is %x2F ("/").
     * - The cookie-path is a prefix of the request-path, and the first character of the request-path that is not
     * included in the cookie-path is a %x2F ("/") character.
     */
    private boolean checkPathsMatch(String cookiePath, String requestPath) {
        return requestPath.equals(cookiePath)
                || (requestPath.startsWith(cookiePath) && cookiePath.charAt(cookiePath.length() - 1) == '/')
                || (requestPath.startsWith(cookiePath) && requestPath.substring(cookiePath.length()).charAt(0) == '/');
    }

}
