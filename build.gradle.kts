import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("org.jetbrains.intellij") version "1.17.3"
    id("org.jetbrains.changelog") version "2.2.0"
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version = properties("platformVersion")
    type = properties("platformType")

    plugins.set(listOf("JavaScript", "Git4Idea"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        version = properties("pluginVersion")
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }

    signPlugin {
        certificateChain = "-----BEGIN CERTIFICATE-----\n" +
                "MIIFwzCCA6ugAwIBAgIUKKaMkMvx9DGjFEhhUXY3+h3s9uAwDQYJKoZIhvcNAQEL\n" +
                "BQAwcTELMAkGA1UEBhMCQkUxFjAUBgNVBAgMDUVhc3QtRmxhbmRlcnMxDTALBgNV\n" +
                "BAcMBEdlbnQxGDAWBgNVBAoMD0Fpa2lkbyBTZWN1cml0eTEMMAoGA1UECwwDRGV2\n" +
                "MRMwEQYDVQQDDAphaWtpZG8uZGV2MB4XDTI0MDczMDE0NTIxM1oXDTI1MDczMDE0\n" +
                "NTIxM1owcTELMAkGA1UEBhMCQkUxFjAUBgNVBAgMDUVhc3QtRmxhbmRlcnMxDTAL\n" +
                "BgNVBAcMBEdlbnQxGDAWBgNVBAoMD0Fpa2lkbyBTZWN1cml0eTEMMAoGA1UECwwD\n" +
                "RGV2MRMwEQYDVQQDDAphaWtpZG8uZGV2MIICIjANBgkqhkiG9w0BAQEFAAOCAg8A\n" +
                "MIICCgKCAgEArYBZTl4e3jusQVMO9j55al73sx5U2d3K1tTC9AO9Cr7LLDtmXkAd\n" +
                "1P3qxeuWUS4YMeJjiBA8d2ad446HmFZg8CogJ4RDAiYEUUgsEI0fVCUlDXmzZou8\n" +
                "Ec5XIhQ29J8AcTYFZDJ41YSIYC1WzApiPleREyPzOGVS8Gl5mcH4/UwUOKEOgdpZ\n" +
                "QGVtdCWk51pAx+vXJuKqLGkMJDpSOFLb0BVgDF92e7IzVTul5EZqP5JeQlZnXMcw\n" +
                "hqRUxi2O/jrLl8B+4oJ86DaiNAO/6jfstdkDVdXSIUu2zuTqHDN0Ez+A1bBec7lh\n" +
                "MiX2L3vJI87yWIjWJ737kRLmBs0O4oNDy7Li+czL+FHP1Asp7d1c+t+bAjybWLA/\n" +
                "/SCzsr7dDtIqKNp+75jzwxiMGNxACM87yJHdLg7+6lffZ5A2AClRnFuQuqqeKjOd\n" +
                "s7c+9chreDXm0/vE3hRq/F6+Fk1KOd39GiloVXh0JY8sFm+fZquctu6DbK8eKmXy\n" +
                "WL2nXWFLBH/j+n2c/yjOjRa+762D5l6DD1kIq5nhK4oVrkBrlY5cuRsroibTDcWm\n" +
                "7QEljyYPfzrWc+hto+Mgs5dY/5Qidk0xmzyUO2L3Qq3+VXMYVbNWEFwynGWiWw27\n" +
                "aGp5CHaHc9nsWlSYoEOdeixvOJcmR7/ErCdniu25A881DA4TiXS17ykCAwEAAaNT\n" +
                "MFEwHQYDVR0OBBYEFEtIOnqMZ3nmiagq17XPEPipqdRzMB8GA1UdIwQYMBaAFEtI\n" +
                "OnqMZ3nmiagq17XPEPipqdRzMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQEL\n" +
                "BQADggIBAFxu/uZqsC4sBp9DJUKfinC823kC4zqMc4XISvGaVpWij/I6hUsHlsdK\n" +
                "mMeVRgNuzCfR4YDlje2aIs9IUxB7hWJyWRDj/hj8bl4VYhkLj8S2Nw8B7RxpmfnI\n" +
                "37Y+0F6TnkFiYwR+fjYxlCQyioOJqYAjJdrPqMA5LrmRChpi1JjPAz4d0QRc+CM7\n" +
                "fc59vzh4nyf4OqUz+FWaODJJlFLbu4aB6MDYtlyP3aM2PKEEsxE0QbyL52Uq/rt8\n" +
                "8FIBQ9WW5/ClqFy4ofdoVAsbmsNUJDMWiLixaOlMIoAKqwf560HZts4K3v+HCWhj\n" +
                "OX2jQLaeTc+hTWWsHnC2ZnKygpZwzgpazS/AdnxFVKUP5YbphpTK/Wtyb4hAKB5K\n" +
                "Qj71mQjcWR09ychhxZYjMxaiONOq4IV0jjNTNRljulFJgJ8GzvxzuMyTHfN3B+wd\n" +
                "Zt3lluqbQWTvDu1z6Y5mVjFtYMPCBH2k+eSxE03Uff5SxFYj8L+PjsrUIwZZcfbH\n" +
                "bV3HcxGUscqJbtMJsO8GxJFh+9rMx2/2ml4hynoL6jdzI9ePpdr3reKf/Ud3QsjQ\n" +
                "84ZjeuBgv1HqIpwaWphNPOpUzQV36PfBrzIdEgVXXGpxr41WFq88pqE3Gn/ICFFn\n" +
                "JC0GFZX+/m/mBI4+ImSXLtikMNp6cya6qYmN3Wiyu2awHfFIyHjG\n" +
                "-----END CERTIFICATE-----\n"
        privateKey = "-----BEGIN PRIVATE KEY-----\n" +
                "MIIJQwIBADANBgkqhkiG9w0BAQEFAASCCS0wggkpAgEAAoICAQCtgFlOXh7eO6xB\n" +
                "Uw72PnlqXvezHlTZ3crW1ML0A70KvsssO2ZeQB3U/erF65ZRLhgx4mOIEDx3Zp3j\n" +
                "joeYVmDwKiAnhEMCJgRRSCwQjR9UJSUNebNmi7wRzlciFDb0nwBxNgVkMnjVhIhg\n" +
                "LVbMCmI+V5ETI/M4ZVLwaXmZwfj9TBQ4oQ6B2llAZW10JaTnWkDH69cm4qosaQwk\n" +
                "OlI4UtvQFWAMX3Z7sjNVO6XkRmo/kl5CVmdcxzCGpFTGLY7+OsuXwH7ignzoNqI0\n" +
                "A7/qN+y12QNV1dIhS7bO5OocM3QTP4DVsF5zuWEyJfYve8kjzvJYiNYnvfuREuYG\n" +
                "zQ7ig0PLsuL5zMv4Uc/UCynt3Vz635sCPJtYsD/9ILOyvt0O0ioo2n7vmPPDGIwY\n" +
                "3EAIzzvIkd0uDv7qV99nkDYAKVGcW5C6qp4qM52ztz71yGt4NebT+8TeFGr8Xr4W\n" +
                "TUo53f0aKWhVeHQljywWb59mq5y27oNsrx4qZfJYvaddYUsEf+P6fZz/KM6NFr7v\n" +
                "rYPmXoMPWQirmeErihWuQGuVjly5GyuiJtMNxabtASWPJg9/OtZz6G2j4yCzl1j/\n" +
                "lCJ2TTGbPJQ7YvdCrf5VcxhVs1YQXDKcZaJbDbtoankIdodz2exaVJigQ516LG84\n" +
                "lyZHv8SsJ2eK7bkDzzUMDhOJdLXvKQIDAQABAoICABel/KY4oHr4hbpSrKGmMszr\n" +
                "nFYC/pxChZ0CG3+AifIOvukswH98vEQ1htxKx+6ROpWEDr5zPhalO0SSvwslzOk3\n" +
                "SLIyLiL7FRASJwVrgtdoh7QeQDRsO+puZkR8HmB50qjbrODzGzK5MZBwZZoCmVD3\n" +
                "hQvnlMV9VBB2Q++P95To24G7fBt5bDxhhslzvxYUoGtPi0Ae2YSbnd7LPz0zmAf0\n" +
                "/eY51c+24pXNv0cNzPoUxHnExqpn8QaNHAv2Fj0zPOBWicTsdcN9/N5bm3s8e6xu\n" +
                "o6x9rDpDFGv/WiSVY88i0vftZfZekG+yKmve8rPJh5FHN29rzv//ZDCNnyeDLkh6\n" +
                "P9hwm4eiC2pbqPU8xB28AVvikLV5cY2y/KzwWIZZHR80pOApsQzwfzYSqLA39WvQ\n" +
                "qCu6wAneenKyuedxp5JFSbHdiVMWppklRuH9j3gLeNScCsjRqTtpRAAXJvDD260P\n" +
                "TrcH0hge5DUXVRp+8MBS4HTWsYqJ6GEKID0rBgUR/TeWB2y15FNxXLJsPxVGh1I/\n" +
                "4X8s8B6tNBMVtyPNtc3IBy5mQe8izBwye1qZ7m6j+OgiebPaLQ9QZIHD+0Sw8pLq\n" +
                "Lke2wuGklA4FPw46fmyyYPRJW0gdapHtFCMEbdM+pbjx5+7KKkBNFez5RDlqMa+1\n" +
                "snGtcYoi6WeeIIGkFgdtAoIBAQDautf/4qjEkhac7T0qcIZwPSrayCtnTNlzDB5b\n" +
                "St0IXYnebEFtkmj8J5OPKbVvfNcGUc1ucLZP0t/saLtWT4fgup7vhlrc8O2i9kHY\n" +
                "MSz09wi4sVXqrBPCM33x+sAAwl4NupC9VtyKBPd/iFA4ZiX+laCiJe5f9tfDzt0O\n" +
                "r5mh6fHir0xvGKv682NykucHxXX/MAxUg/t0CWMpUw7SkQfx8vFFr+ClUAxrPFaw\n" +
                "C+F7mkX6tf8W80HXq4Zj9qi6o1HcBoZs10wjgV7M1y7uayxgjqsEts4cd/6hxYJb\n" +
                "tjLVylGqVXplniE0kuzWXeqFQNxFPUjcIxp1/oVGgTyePil9AoIBAQDLEJrmDZBr\n" +
                "Ff5IZ+VfyDkjDaEMMLtR2PLQdPaqcd6ES8F14ku9VuqgrSdl1zrqtusfktik+b2+\n" +
                "HXKbS2IclNmVUzaG809DmOXO0JR1ZBi+nYb1OnCdS7L0nJx2/mc8FXlMWsyh63/x\n" +
                "2VlfH096uMarHJGiDERvpiVN3V1jM8OJWMiurIk2KMppqeT5Ikogm+N4eaOJKWmu\n" +
                "PUqPE2Gs1wiul6kQh3aAAHjIE4+s28kujQPanv7A0SDNzeVjlWmBPQlL4pbnkEWK\n" +
                "HfjEyb0usScBuAcfHjhB/xanzntORXAlXuUWxGgoqo4ORa+oltKWfAxe6VfCYaD4\n" +
                "/ZhpLk163OwdAoIBAGm9HaKELlYo0Wm3fnQ/xZ4I0+jED8d2bUCiwSIdBNGAdp/h\n" +
                "8ZdIyhvr/fedkCp/TWuurAMR1fCs3rdowfetpuBLF35vkwlwJ1E6fwZGb5dUvRCc\n" +
                "Gg7CdSLSxbXvJFa5n8I/SK1fimnmkMdEXJOHPiLerrL0Z1JZAGGWZWMuHLUbZWXS\n" +
                "nL6wTKOaeZ7vyWQGX06oKa3JyAuGZjuZ/XZpyk96xIyNw6nnErnch4O/mpeoMuBK\n" +
                "jzDHFDNwph05JsRjI7WcKewAALzBU9Tuya9UfDWJgr9aqDU3BQ+rbaNsZxLXJbeU\n" +
                "bwEk+qRxyrCu4irc+h82yzwwdCBKlJ9K5sURA/kCggEBAK2zWo0HdU3k1kshdHfm\n" +
                "UdnGQsimttVgevQN2SIbnXgVRTuNg7RGsRUYiOb4oRE9vwqXFnKonsINdoeYiChi\n" +
                "u2ycbbwqTNdJ5upuSU/Re8kx/suuXb5vjnjnrn+rRquCwuupj/nB9QPwJB7WLaqQ\n" +
                "fIDKQ1kWnUPUH3znqoa9NLo8FA420HhflCWmipMB3d7e+kmH43fk0N2W2DxjG945\n" +
                "YtfWK2xUM+1yed7LvM7kgscrIOzVrJ1LRoBFa78vg7xldZpQDT2vj4yB7taGqylR\n" +
                "qEg0HwNjWkQ57CtG2PsvMqsE7Xn4MBD5M+LJgWT4/tJy8jIN8F/6GzRh2Qm/7c73\n" +
                "Q5ECggEBAKROeP7NP0SjtK8zYAQnyb0mBwOVQzmDVBiQzU58EBkA4V1DjOykay8z\n" +
                "qtUOQaZcAiNDyK2MJwb9nFUk97rUy8JCQHEFJznyGB1gWMf8Q5psgCagDgW4bqJT\n" +
                "DAl/irMuefz7oUJA8jNgJHdYo4JpYPb4WSaf2a3P6GjrzSn/7FdKA3MjNuC3V3Ot\n" +
                "UlhYQm8ERTYra+InOtzBs4nzu3RHAPSyNuWm+9YE6iUL9kPNapiHTGU1ffEK2N/k\n" +
                "jwQ1rM371lidq0idCOOdlhf1hpKuKsb4d98Y5GHkZ/I51WLMyB36k5BQUlRNSR1q\n" +
                "YrVF1iufplqbWvJag2Eq6p7+dg6WtGw=\n" +
                "-----END PRIVATE KEY-----\n"
        password = "Aikido9050!"
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    prepareSandbox {
        notCompatibleWithConfigurationCache("Uses project copy")
        doLast {
            exec {
                commandLine(
                    "./setup-lspjs.sh"
                )
            }
            copy {
                from("${project.projectDir}/src/main/kotlin/dev/aikido/plugin/services/sast/semgrep/lspjs")
                into("${destinationDir.path}/${intellij.pluginName.get()}/lspjs/dist")
            }
            copy {
                from("${project.projectDir}/src/main/kotlin/dev/aikido/plugin/services/secrets/gitleaks/dist")
                into("${destinationDir.path}/${intellij.pluginName.get()}/gitleaks/dist")
            }
            copy {
                from("${project.projectDir}/src/main/kotlin/dev/aikido/plugin/services/secrets/gitleaks/config")
                into("${destinationDir.path}/${intellij.pluginName.get()}/gitleaks/config")
            }
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Needed for local development only
    //implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.22.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.nimbusds:nimbus-jose-jwt:9.39.3")
}
