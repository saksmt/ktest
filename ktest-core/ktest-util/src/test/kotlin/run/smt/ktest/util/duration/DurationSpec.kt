package run.smt.ktest.util.duration

import io.kotlintest.matchers.shouldEqual
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import io.kotlintest.specs.ShouldSpec
import java.time.Duration

class TimeConversionSpec : ShouldSpec({
    "inNanos" {
        should("show correct time") {
            inNanos { (1L.nanos() + 2L.millis() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()).show() } shouldEqual
                "6D 5h 4m 3s 2ms 1n"
        }

        should("compute correct time") {
            inNanos { 1L.nanos() + 2L.millis() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()  } shouldEqual 536643002000001L
        }
    }

    "inMillis" {
        should("show correct time") {
            inMillis { (1L.nanos() + 2L.millis() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()).show() } shouldEqual
                "6D 5h 4m 3s 2ms"
        }

        should("compute correct time") {
            inMillis { 1L.nanos() + 2L.millis() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()  } shouldEqual 536643002L
        }

        "overflowed with nanos" {
            should("show correct time") {
                inMillis { (1000000L.nanos() + 2L.millis() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()).show() } shouldEqual
                    "6D 5h 4m 3s 3ms"
            }

            should("compute correct time") {
                inMillis { 1000000L.nanos() + 2L.millis() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()  } shouldEqual 536643003L
            }
        }
    }

    "inSeconds" {
        should("show correct time") {
            inSeconds { (1L.nanos() + 2L.millis() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()).show() } shouldEqual
                "6D 5h 4m 3s"
        }

        should("compute correct time") {
            inSeconds { 1L.nanos() + 2L.millis() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()  } shouldEqual 536643L
        }

        "with overflow" {
            "by millis" {
                should("show correct time") {
                    inSeconds { (1000L.millis() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()).show() } shouldEqual
                        "6D 5h 4m 4s"
                }

                should("compute correct time") {
                    inSeconds { 1000L.millis() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days() } shouldEqual 536644L
                }
            }

            "by nanos" {
                should("show correct time") {
                    inSeconds { (1000_000000L.nanos() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()).show() } shouldEqual
                        "6D 5h 4m 4s"
                }

                should("compute correct time") {
                    inSeconds { 1000_000000L.nanos() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days() } shouldEqual 536644L
                }
            }
        }
    }

    "inMinutes" {
        should("show correct time") {
            inMinutes { (1L.nanos() + 2L.millis() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()).show() } shouldEqual
                "6D 5h 4m"
        }

        should("compute correct time") {
            inMinutes { 1L.nanos() + 2L.millis() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()  } shouldEqual 8944L
        }

        "with overflow" {
            "by seconds" {
                should("show correct time") {
                    inMinutes { (60L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()).show() } shouldEqual
                        "6D 5h 5m"
                }

                should("compute correct time") {
                    inMinutes { 60L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()  } shouldEqual 8945L
                }
            }

            "by millis" {
                should("show correct time") {
                    inMinutes { (60000L.millis() + 4L.minutes() + 5L.hours() + 6L.days()).show() } shouldEqual
                        "6D 5h 5m"
                }

                should("compute correct time") {
                    inMinutes { 60000L.millis() + 4L.minutes() + 5L.hours() + 6L.days()  } shouldEqual 8945L
                }
            }

            "by nanos" {
                should("show correct time") {
                    inMinutes { (60_000_000000L.nanos() + 4L.minutes() + 5L.hours() + 6L.days()).show() } shouldEqual
                        "6D 5h 5m"
                }

                should("compute correct time") {
                    inMinutes { 60_000_000000L.nanos() + 4L.minutes() + 5L.hours() + 6L.days()  } shouldEqual 8945L
                }
            }
        }
    }

    "inHours" {
        should("show correct time") {
            inHours { (1L.nanos() + 2L.millis() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()).show() } shouldEqual
                "6D 5h"
        }

        should("compute correct time") {
            inHours { 1L.nanos() + 2L.millis() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()  } shouldEqual 149L
        }

        "with overflow" {
            "by minutes" {
                should("show correct time") {
                    inHours { (60L.minutes() + 5L.hours() + 6L.days()).show() } shouldEqual
                        "6D 6h"
                }

                should("compute correct time") {
                    inHours { 60L.minutes() + 5L.hours() + 6L.days()  } shouldEqual 150L
                }
            }

            "by seconds" {
                should("show correct time") {
                    inHours { (3600L.seconds() + 5L.hours() + 6L.days()).show() } shouldEqual
                        "6D 6h"
                }

                should("compute correct time") {
                    inHours { 3600L.seconds() + 5L.hours() + 6L.days()  } shouldEqual 150L
                }
            }

            "by millis" {
                should("show correct time") {
                    inHours { (3600_000L.millis() + 5L.hours() + 6L.days()).show() } shouldEqual
                        "6D 6h"
                }

                should("compute correct time") {
                    inHours { 3600_000L.millis() + 5L.hours() + 6L.days()  } shouldEqual 150L
                }
            }

            "by nanos" {
                should("show correct time") {
                    inHours { (3600_000_000000L.nanos() + 5L.hours() + 6L.days()).show() } shouldEqual
                        "6D 6h"
                }

                should("compute correct time") {
                    inHours { 3600_000_000000L.nanos() + 5L.hours() + 6L.days()  } shouldEqual 150L
                }
            }
        }
    }

    "inDays" {
        should("show correct time") {
            inDays { (1L.nanos() + 2L.millis() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()).show() } shouldEqual
                "6D"
        }

        should("compute correct time") {
            inDays { 1L.nanos() + 2L.millis() + 3L.seconds() + 4L.minutes() + 5L.hours() + 6L.days()  } shouldEqual 6L
        }

        "with overflow" {
            "by hours" {
                should("show correct time") {
                    inDays { (24L.hours() + 6L.days()).show() } shouldEqual
                        "7D"
                }

                should("compute correct time") {
                    inDays { 24L.hours() + 6L.days()  } shouldEqual 7L
                }
            }

            "by minutes" {
                should("show correct time") {
                    inDays { (1440L.minutes() + 6L.days()).show() } shouldEqual
                        "7D"
                }

                should("compute correct time") {
                    inDays { 1440L.minutes() + 6L.days()  } shouldEqual 7L
                }
            }

            "by seconds" {
                should("show correct time") {
                    inDays { (86400L.seconds() + 6L.days()).show() } shouldEqual
                        "7D"
                }

                should("compute correct time") {
                    inDays { 86400L.seconds() + 6L.days()  } shouldEqual 7L
                }
            }

            "by millis" {
                should("show correct time") {
                    inDays { (86400_000L.millis() + 6L.days()).show() } shouldEqual
                        "7D"
                }

                should("compute correct time") {
                    inDays { 86400_000L.millis() + 6L.days()  } shouldEqual 7L
                }
            }

            "by nanos" {
                should("show correct time") {
                    inDays { (86400_000_000000L.nanos() + 6L.days()).show() } shouldEqual
                        "7D"
                }

                should("compute correct time") {
                    inDays { 86400_000_000000L.nanos() + 6L.days()  } shouldEqual 7L
                }
            }
        }
    }

    "toDuration" {
        should("return duration in days") {
            inDays {
                (0.nanos()
                    + 0.millis()
                    + 0.seconds()
                    + 0.minutes()
                    + 0.hours()
                    + 4.days()).toDuration()
            } shouldEqual Duration.ofDays(4)
        }

        should("return duration in hours") {
            inHours {
                (0.nanos()
                    + 0.millis()
                    + 0.seconds()
                    + 0.minutes()
                    + 4.hours()
                    + 0.days()).toDuration()
            } shouldEqual Duration.ofHours(4)
        }

        should("return duration in minutes") {
            inMinutes {
                (0.nanos()
                    + 0.millis()
                    + 0.seconds()
                    + 4.minutes()
                    + 0.hours()
                    + 0.days()).toDuration()
            } shouldEqual Duration.ofMinutes(4)
        }

        should("return duration in seconds") {
            inSeconds {
                (0.nanos()
                    + 0.millis()
                    + 4.seconds()
                    + 0.minutes()
                    + 0.hours()
                    + 0.days()).toDuration()
            } shouldEqual Duration.ofSeconds(4)
        }

        should("return duration in millis") {
            inMillis {
                (0.nanos()
                    + 4.millis()
                    + 0.seconds()
                    + 0.minutes()
                    + 0.hours()
                    + 0.days()).toDuration()
            } shouldEqual Duration.ofMillis(4)
        }

        should("return duration in nanos") {
            inNanos {
                (4.nanos()
                    + 0.millis()
                    + 0.seconds()
                    + 0.minutes()
                    + 0.hours()
                    + 0.days()).toDuration()
            } shouldEqual Duration.ofNanos(4)
        }
    }

    "logical rules" {
        should("not be fucked up") {
            forAll(Gen.choose(0L, 20L)) { n: Long ->
                inDays { inHours { inMinutes { inSeconds { inMillis { inNanos { n.days() }.nanos() }.millis() }.seconds() }.minutes() }.hours() } == n
            }
        }
    }
})
