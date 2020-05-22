/*
 * Copyright © 2020 NHSX. All rights reserved.
 */

package uk.nhs.nhsx.sonar.android.app.status

import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone.UTC
import org.junit.Test
import uk.nhs.nhsx.sonar.android.app.util.nonEmptySetOf

class UserStateSerializationTest {

    val serialize = UserStateSerialization::serialize
    val deserialize = UserStateSerialization::deserialize

    @Test
    fun `serialize default state`() {

        assertThat(serialize(DefaultState()))
            .isEqualTo("""{"type":"DefaultState","testInfo":"null"}""")
    }

    @Test
    fun `serialize default state with test result`() {
        val testDate = DateTime(15872413022578L, UTC)
        assertThat(
            serialize(
                DefaultState(
                    TestInfo(
                        TestResult.NEGATIVE,
                        testDate,
                        stateChanged = true,
                        dismissed = false
                    )
                )
            )
        )
            .isEqualTo("""{"type":"DefaultState","testInfo":"{\"result\":\"NEGATIVE\",\"dismissed\":false,\"stateChanged\":true,\"testDate\":15872413022578}"}""")
    }

    @Test
    fun `serialize recovery state`() {

        assertThat(serialize(RecoveryState()))
            .isEqualTo("""{"type":"RecoveryState","testInfo":"null"}""")
    }

    @Test
    fun `serialize recovery state with test result`() {
        val testDate = DateTime(15872413022578L, UTC)
        assertThat(
            serialize(
                RecoveryState(
                    TestInfo(
                        TestResult.NEGATIVE,
                        testDate,
                        stateChanged = true,
                        dismissed = false
                    )
                )
            )
        )
            .isEqualTo("""{"type":"RecoveryState","testInfo":"{\"result\":\"NEGATIVE\",\"dismissed\":false,\"stateChanged\":true,\"testDate\":15872413022578}"}""")
    }

    @Test
    fun `serialize amber state`() {
        val until = DateTime(1587241302262L, UTC)

        assertThat(
            serialize(
                AmberState(
                    until
                )
            )
        )
            .isEqualTo(
                """{"type":"AmberState","testInfo":"null","until":1587241302262}"""
            )
    }

    @Test
    fun `serialize amber state with test result`() {
        val until = DateTime(1587241302262L, UTC)
        val testDate = DateTime(15872413022578L, UTC)

        assertThat(
            serialize(
                AmberState(
                    until, TestInfo(
                        TestResult.NEGATIVE,
                        testDate,
                        stateChanged = true,
                        dismissed = false
                    )
                )
            )
        )
            .isEqualTo(
                """{"type":"AmberState","testInfo":"{\"result\":\"NEGATIVE\",\"dismissed\":false,\"stateChanged\":true,\"testDate\":15872413022578}","until":1587241302262}"""
            )
    }

    @Test
    fun `serialize red state`() {
        val until = DateTime(1587241302263L, UTC)
        val symptoms = nonEmptySetOf(Symptom.COUGH, Symptom.TEMPERATURE)

        assertThat(serialize(RedState(until, symptoms)))
            .isEqualTo(
                """{"symptoms":["COUGH","TEMPERATURE"],"testInfo":"null","until":1587241302263,"type":"RedState","symptomsStartDate":-1}"""
            )
    }

    @Test
    fun `serialize red state with symptoms start date`() {
        val until = DateTime(1587241302263L, UTC)
        val symptoms = nonEmptySetOf(Symptom.TEMPERATURE)
        val symptomsStartDate = DateTime(1587241303263L, UTC)
        assertThat(serialize(RedState(until, symptoms, symptomsStartDate)))
            .isEqualTo(
                """{"symptoms":["TEMPERATURE"],"testInfo":"null","until":1587241302263,"type":"RedState","symptomsStartDate":1587241303263}"""
            )
    }

    @Test
    fun `serialize checkin state`() {
        val until = DateTime(1587241302263L, UTC)
        val symptoms = nonEmptySetOf(Symptom.TEMPERATURE)

        assertThat(serialize(CheckinState(until, symptoms)))
            .isEqualTo(
                """{"symptoms":["TEMPERATURE"],"testInfo":"null","until":1587241302263,"type":"CheckinState","symptomsStartDate":-1}"""
            )
    }

    @Test
    fun `serialize checkin state with symptoms start date`() {
        val until = DateTime(1587241302263L, UTC)
        val symptoms = nonEmptySetOf(Symptom.TEMPERATURE)
        val symptomsStartDate = DateTime(1587241303263L, UTC)

        assertThat(serialize(CheckinState(until, symptoms, symptomsStartDate)))
            .isEqualTo(
                """{"symptoms":["TEMPERATURE"],"testInfo":"null","until":1587241302263,"type":"CheckinState","symptomsStartDate":1587241303263}"""
            )
    }

    @Test
    fun `deserialize null`() {
        assertThat(deserialize(null))
            .isEqualTo(DefaultState())
    }

    @Test
    fun `deserialize default state`() {
        assertThat(deserialize("""{"type":"DefaultState","testInfo":"null"}"""))
            .isEqualTo(DefaultState())
    }

    @Test
    fun `deserialize default state - with legacy until timestamp`() {
        assertThat(deserialize("""{"until":1587241302261,"type":"DefaultState"}"""))
            .isEqualTo(DefaultState())
    }

    @Test
    fun `deserialize recovery state`() {
        assertThat(deserialize("""{"type":"RecoveryState"}"""))
            .isEqualTo(RecoveryState())
    }

    @Test
    fun `deserialize recovery state - with legacy until timestamp`() {
        assertThat(deserialize("""{"until":1587241302262,"type":"RecoveryState"}"""))
            .isEqualTo(RecoveryState())
    }

    @Test
    fun `deserialize ember(typo) state`() {
        val until = DateTime(1587241302262L, UTC)

        assertThat(deserialize("""{"until":1587241302262,"type":"EmberState"}"""))
            .isEqualTo(AmberState(until))
    }

    @Test
    fun `deserialize amber state`() {
        val until = DateTime(1587241302262L, UTC)

        assertThat(deserialize("""{"until":1587241302262,"type":"AmberState"}"""))
            .isEqualTo(AmberState(until))
        assertThat(deserialize("""{"type":"AmberState","testInfo":"null","until":1587241302262}"""))
            .isEqualTo(AmberState(until))
    }

    @Test
    fun `deserialize amber state with test result`() {
        val until = DateTime(1587241302262L, UTC)
        val testDate = DateTime(15872413022578L, UTC)

        val state = AmberState(
            until, TestInfo(
                TestResult.NEGATIVE,
                testDate,
                stateChanged = true,
                dismissed = false
            )
        )
        assertThat(
            deserialize(
                """{
            "type":"AmberState",
            "testInfo":"{\"result\":\"NEGATIVE\",\"testDate\":15872413022578,\"stateChanged\":true,\"dismissed\":false}",
            "until":1587241302262
            }"""
            )
        )
            .isEqualTo(state)
    }

    @Test
    fun `deserialize red state`() {
        val until = DateTime(1587241302262L, UTC)

        assertThat(deserialize("""{"until":1587241302262,"symptoms":["COUGH"],"type":"RedState"}"""))
            .isEqualTo(RedState(until, nonEmptySetOf(Symptom.COUGH)))
    }

    @Test
    fun `deserialize red state with test result`() {
        val until = DateTime(1587241302262L, UTC)
        val testDate = DateTime(15872413022578L, UTC)

        val state = RedState(
            until,
            nonEmptySetOf(Symptom.COUGH),
            testInfo = TestInfo(
                TestResult.NEGATIVE,
                testDate,
                stateChanged = true,
                dismissed = false
            )
        )
        assertThat(
            deserialize(
                """{
            "type":"RedState",
            "symptoms":["COUGH"],
            "testInfo":"{\"result\":\"NEGATIVE\",\"testDate\":15872413022578,\"stateChanged\":true,\"dismissed\":false}",
            "until":1587241302262
            }"""
            )
        )
            .isEqualTo(state)
    }

    @Test
    fun `deserialize red state with symptoms date`() {
        val until = DateTime(1587241302262L, UTC)

        val state = RedState(
            until,
            nonEmptySetOf(Symptom.COUGH),
            symptomsStartDate = until
        )
        assertThat(
            deserialize(
                """{
            "type":"RedState",
            "symptoms":["COUGH"],
            "symptomsStartDate":1587241302262,
            "until":1587241302262
            }"""
            )
        )
            .isEqualTo(state)
    }

    @Test
    fun `deserialize checkin state`() {
        val until = DateTime(1587241302262L, UTC)

        assertThat(deserialize("""{"until":1587241302262,"symptoms":["COUGH"],"type":"CheckinState"}"""))
            .isEqualTo(CheckinState(until, nonEmptySetOf(Symptom.COUGH)))
    }

    @Test
    fun `deserialize invalid red state`() {
        assertThat(deserialize("""{"until":1587241302262,"symptoms":[],"type":"RedState"}"""))
            .isEqualTo(DefaultState())
    }
}
