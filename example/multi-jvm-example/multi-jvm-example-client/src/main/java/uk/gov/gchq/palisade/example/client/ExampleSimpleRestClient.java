/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.example.client;

import uk.gov.gchq.koryphe.impl.predicate.CollectionContains;
import uk.gov.gchq.koryphe.impl.predicate.IsMoreThan;
import uk.gov.gchq.koryphe.impl.predicate.Not;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.client.SimpleRestClient;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.example.ExampleObj;
import uk.gov.gchq.palisade.example.data.serialiser.ExampleObjSerialiser;
import uk.gov.gchq.palisade.example.rule.If;
import uk.gov.gchq.palisade.example.rule.IsVisible;
import uk.gov.gchq.palisade.example.rule.SetValue;
import uk.gov.gchq.palisade.policy.service.Policy;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.request.SetPolicyRequest;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.service.request.SimpleConnectionDetail;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.request.AddUserRequest;

import java.util.stream.Stream;

import static uk.gov.gchq.palisade.Util.project;
import static uk.gov.gchq.palisade.Util.select;

public class ExampleSimpleRestClient extends SimpleRestClient<ExampleObj> {
    public static final String RESOURCE_TYPE = "exampleObj";

    public ExampleSimpleRestClient() {
        super();
        initialiseRestServices();
    }

    public Stream<ExampleObj> read(final String filename, final String userId, final String justification) {
        return super.read(filename, RESOURCE_TYPE, userId, justification);
    }

    @Override
    protected Serialiser<?, ExampleObj> createSerialiser() {
        return new ExampleObjSerialiser();
    }

    private void initialiseRestServices() {
        // The user authorisation owner or sys admin needs to add the user
        final UserService userService = createUserService();
        userService.addUser(
                new AddUserRequest(
                        new User()
                                .userId("Alice")
                                .auths("public", "private")
                                .roles("user", "admin")
                )
        );
        userService.addUser(
                new AddUserRequest(
                        new User()
                                .userId("Bob")
                                .auths("public")
                                .roles("user")
                )
        );

        // The policy owner or sys admin needs to add the policies
        final PolicyService policyService = createPolicyService();
        policyService.setPolicy(
                new SetPolicyRequest(
                        new FileResource("file1", RESOURCE_TYPE),
                        new Policy<ExampleObj>()
                                .message("Visibility, ageOff and property redaction")
                                .rule(
                                        "1-visibility",
                                        select("Record.visibility", "User.auths"),
                                        new IsVisible()
                                )
                                .rule(
                                        "2-ageOff",
                                        select("Record.timestamp"),
                                        new IsMoreThan(12L)
                                )
                                .rule(
                                        "3-redactProperty",
                                        select("User.roles", "Record.property"),
                                        new If<>().predicate(0, new Not<>(new CollectionContains("admin")))
                                                .then(1, new SetValue<>("redacted"), 1),
                                        project("User.roles", "Record.property")
                                )
                )
        );

        // The sys admin needs to add the resources
        final ResourceService resourceService = super.createResourceService();
        resourceService.addResource(new AddResourceRequest(
                new DirectoryResource("dir1", RESOURCE_TYPE),
                new FileResource("file1", RESOURCE_TYPE),
                new SimpleConnectionDetail()
        ));

        // Wait for the users, policies and resources to be loaded
        try {
            Thread.sleep(1000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }
}
