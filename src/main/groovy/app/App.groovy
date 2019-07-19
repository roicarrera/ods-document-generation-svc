package app

import com.typesafe.config.ConfigFactory

import groovy.json.JsonSlurper

import org.jooby.Jooby
import org.jooby.MediaType
import org.jooby.json.Jackson

import static org.jooby.JoobyExtension.post

class App extends Jooby {

    {
        use(new Jackson())
        use(new DocGen())

        post(this, "/document", { req, rsp ->
            validateEnvironmentVars()

            def body = new JsonSlurper().parseText(req.body().value())
            validateRequestParams(body)

            def pdf = new DocGen().generate(body.metadata.type, body.metadata.version, body.data)
            rsp.send([
                data: pdf.encodeBase64().toString()
            ])
        })
        .consumes(MediaType.json)
        .produces(MediaType.json)
    }

    private static void validateEnvironmentVars() {
        if (!System.getenv("BITBUCKET_URL")) {
            throw new IllegalArgumentException("missing environment variable 'BITBUCKET_URL'")
        }

        if (!System.getenv("BITBUCKET_USERNAME")) {
            throw new IllegalArgumentException("missing environment variable 'BITBUCKET_USERNAME'")
        }

        if (!System.getenv("BITBUCKET_PASSWORD")) {
            throw new IllegalArgumentException("missing environment variable 'BITBUCKET_PASSWORD'")
        }

        if (!System.getenv("BITBUCKET_DOCUMENT_TEMPLATES_PROJECT")) {
            throw new IllegalArgumentException("missing environment variable 'BITBUCKET_DOCUMENT_TEMPLATES_PROJECT'")
        }

        if (!System.getenv("BITBUCKET_DOCUMENT_TEMPLATES_REPO")) {
            throw new IllegalArgumentException("missing environment variable 'BITBUCKET_DOCUMENT_TEMPLATES_REPO'")
        }
    }

    private static void validateRequestParams(Map body) {
        if (body?.metadata?.type == null) {
            throw new IllegalArgumentException("missing argument 'metadata.type'")
        }

        if (body?.metadata?.version == null) {
            throw new IllegalArgumentException("missing argument 'metadata.version'")
        }

        if (body?.data == null) {
            throw new IllegalArgumentException("missing argument 'data'")
        }
    }

    static void main(String... args) {
        ConfigFactory.invalidateCaches()
        run(App.class, args)
    }
}
