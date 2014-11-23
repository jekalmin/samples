package com.tistory.jekalmin;

import java.util.List;
import java.util.Map;

import org.cloudfoundry.identity.uaa.client.PreAuthenticatedPrincipalSource;
import org.cloudfoundry.identity.uaa.client.SocialClientUserDetails;
import org.cloudfoundry.identity.uaa.client.SocialClientUserDetailsSource;
import org.cloudfoundry.identity.uaa.user.UaaAuthority;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;
import org.springframework.web.client.RestOperations;

/**
 * 페이스북용 이름 가져오는 방법 추가
 * 원본인 {@link SocialClientUserDetailsSource}와 다른 점은 {@link MySocialClientUserDetailsSource#getUserName(Map)} 에서
 *
 * <code>
 * if (this.userInfoUrl.contains("facebook.com")) {
 *     key = "name";
 * }
 * </code>
 *
 * 이 부분이 추가되었다. 메쏘드와 필드들이 private이여서 상속으로 추가 못하고, 재구현하였다.
 * 
 * @author jekalmin
 *
 */
public class MySocialClientUserDetailsSource
        implements InitializingBean, PreAuthenticatedPrincipalSource<Authentication>
{
	private RestOperations restTemplate;
	private String userInfoUrl;

	public void setRestTemplate(RestOperations restTemplate)
	{
		this.restTemplate = restTemplate;
	}

	public void setUserInfoUrl(String userInfoUrl)
	{
		this.userInfoUrl = userInfoUrl;
	}

	@Override
	public void afterPropertiesSet()
	{
		Assert.state(this.userInfoUrl != null, "User info URL must be provided");
		Assert.state(this.restTemplate != null, "RestTemplate URL must be provided");
	}

	@Override
	public Authentication getPrincipal()
	{
		Map map = this.restTemplate.getForObject(this.userInfoUrl, Map.class, new Object[0]);
		String userName = getUserName(map);
		String email = null;
		if (map.containsKey("email")) {
			email = (String) map.get("email");
		}
		if ((userName == null) && (email != null)) {
			userName = email;
		}
		if (userName == null) {
			userName = (String) map.get("id");
		}
		List authorities = UaaAuthority.USER_AUTHORITIES;
		SocialClientUserDetails user = new SocialClientUserDetails(userName, authorities);
		user.setSource(SocialClientUserDetails.Source.classify(this.userInfoUrl));
		user.setExternalId(getUserId(map));
		String fullName = getFullName(map);
		if (fullName != null) {
			user.setFullName(fullName);
		}
		if (email != null) {
			user.setEmail(email);
		}
		return user;
	}

	private String getFullName(Map<String, String> map) {
		if (map.containsKey("name")) {
			return (map.get("name"));
		}
		if (map.containsKey("formattedName")) {
			return (map.get("formattedName"));
		}
		if (map.containsKey("fullName")) {
			return (map.get("fullName"));
		}
		String firstName = null;
		if (map.containsKey("firstName")) {
			firstName = map.get("firstName");
		}
		if (map.containsKey("givenName")) {
			firstName = map.get("givenName");
		}
		String lastName = null;
		if (map.containsKey("lastName")) {
			lastName = map.get("lastName");
		}
		if (map.containsKey("familyName")) {
			lastName = map.get("familyName");
		}
		if ((firstName != null) &&
		        (lastName != null)) {
			return firstName + " " + lastName;
		}

		return null;
	}

	private Object getUserId(Map<String, String> map) {
		String key = "id";
		if (this.userInfoUrl.contains("cloudfoundry.com")) {
			key = "user_id";
		}
		return map.get(key);
	}

	private String getUserName(Map<String, String> map) {
		String key = "username";
		if (map.containsKey(key)) {
			return (map.get(key));
		}
		if ((this.userInfoUrl.contains("cloudfoundry.com")) || (this.userInfoUrl.endsWith("/uaa/userinfo"))) {
			key = "user_name";
		}
		if (this.userInfoUrl.contains("github.com")) {
			key = "login";
		}
		if (this.userInfoUrl.contains("twitter.com")) {
			key = "screen_name";
		}
		if (this.userInfoUrl.contains("facebook.com")) {
			key = "name";
		}
		String value = map.get(key);
		return value;
	}
}