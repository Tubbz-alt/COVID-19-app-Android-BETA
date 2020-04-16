/*
 * Copyright © 2020 NHSX. All rights reserved.
 */

package uk.nhs.nhsx.sonar.android.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_flow_test_start.start_main_activity
import org.jetbrains.annotations.TestOnly
import uk.nhs.nhsx.sonar.android.app.onboarding.OnboardingStatusProvider
import uk.nhs.nhsx.sonar.android.app.registration.SonarIdProvider
import uk.nhs.nhsx.sonar.android.app.status.StatusStorage
import uk.nhs.nhsx.sonar.android.client.KeyStorage
import javax.inject.Inject

@TestOnly
class FlowTestStartActivity : AppCompatActivity() {

    @Inject
    lateinit var statusStorage: StatusStorage

    @Inject
    lateinit var keyStorage: KeyStorage

    @Inject
    lateinit var sonarIdProvider: SonarIdProvider

    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var onboardingStatusProvider: OnboardingStatusProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        setContentView(R.layout.activity_flow_test_start)

        start_main_activity.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
