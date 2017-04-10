#!groovy
node {
    timeout(time:1, unit:'HOURS') {
        // Checkout code from repository
        stage 'Checkout'
        checkout scm

        // Run the maven build
        stage 'Build'
        sh "mvn clean package"
        stash 'source'

        stage 'Deploy-artifact'
        if (env.BRANCH_NAME == 'master') {
            echo "On branch develop -- running deploy to Nexus"
            // Deploy artifact to Nexus
            unstash 'source'
            sh "mvn deploy"
        } else {
            echo "Not on develop branch -- skipping deploy"
        }
    }
}