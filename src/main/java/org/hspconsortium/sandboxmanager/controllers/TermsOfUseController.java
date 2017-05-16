/*
 * #%L
 *
 * %%
 * Copyright (C) 2014 - 2015 Healthcare Services Platform Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.hspconsortium.sandboxmanager.controllers;

import org.hspconsortium.sandboxmanager.model.SystemRole;
import org.hspconsortium.sandboxmanager.model.TermsOfUse;
import org.hspconsortium.sandboxmanager.model.User;
import org.hspconsortium.sandboxmanager.services.OAuthService;
import org.hspconsortium.sandboxmanager.services.TermsOfUseService;
import org.hspconsortium.sandboxmanager.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Date;

@RestController
@RequestMapping("/REST/termsofuse")
public class TermsOfUseController extends AbstractController  {
    private static Logger LOGGER = LoggerFactory.getLogger(TermsOfUseController.class.getName());

    private final TermsOfUseService termsOfUseService;
    private final UserService userService;

    @Inject
    public TermsOfUseController(final TermsOfUseService termsOfUseService, final OAuthService oAuthService,
                                final UserService userService) {
        super(oAuthService);
        this.termsOfUseService = termsOfUseService;
        this.userService = userService;
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.GET, produces ="application/json")
    public TermsOfUse getLatestTermsOfUse() {
        return termsOfUseService.orderByCreatedTimestamp().get(0);
    }

    @RequestMapping(method = RequestMethod.POST, produces ="application/json")
    public TermsOfUse createTermsOfUse(HttpServletRequest request, @RequestBody final TermsOfUse termsOfUse) {
        User user = userService.findBySbmUserId(getSystemUserId(request));
        checkUserSystemRole(user, SystemRole.ADMIN);

        termsOfUse.setCreatedTimestamp(new Timestamp(new Date().getTime()));
        return termsOfUseService.save(termsOfUse);
    }
}
