package de.clashsoft.gradle.angular

import groovy.util.logging.Slf4j
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskAction

@Slf4j
class InstallAngularDependenciesTask extends Exec {

	@TaskAction
	@Override
	protected void exec() {
		log.debug("Installing Angular dependencies. Working Directory: {} Execute: {}", this.workingDir, this.commandLine.join(' '))
		def stdout = new ByteArrayOutputStream()
		def stderr = new ByteArrayOutputStream()
		super.standardOutput = stdout
		super.errorOutput = stderr
		super.exec()
		int exitCode = getExecutionResult().get().exitValue
		log.debug("Exit code: {}", exitCode)
		log.debug("Stdout: {}", new String(stdout.toByteArray()))
		log.debug("Stderr: {}", new String(stderr.toByteArray()))
		if (exitCode != 0) {
			throw new RuntimeException("Failed to install Angular dependencies. Exit code: " + exitCode)
		}
	}
}
