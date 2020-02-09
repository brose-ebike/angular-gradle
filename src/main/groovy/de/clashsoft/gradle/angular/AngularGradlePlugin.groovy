package de.clashsoft.gradle.angular

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin

class AngularGradlePlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		final AngularGradleConfig config = project.extensions.create('angular', AngularGradleConfig)

		project.tasks.register('installAngularDependencies', InstallAngularDependenciesTask) {
			it.group = BasePlugin.BUILD_GROUP
			it.workingDir = config.appDir

			// up-to-date checks cause problems because node_modules is so huge :(
			// just hope the package manager figures out when it does not need to do anything
			// it.inputs.file "$appDir/package.json"
			// it.outputs.files "$appDir/package-lock.json", "$appDir/yarn.lock", "$appDir/pnpm-lock.yaml" // lock files
			// it.outputs.dir "$appDir/node_modules"

			it.executable = mkCmd(config.packageManager.get())
			it.args('install')
			it.args(config.packageManagerArgs)
		}
	}

	private static String mkCmd(String stringProperty) {
		return isWindows() ? stringProperty + '.cmd' : stringProperty
	}

	private static boolean isWindows() {
		return System.getProperty('os.name').toUpperCase().contains('WINDOWS')
	}
}
