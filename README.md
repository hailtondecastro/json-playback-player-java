# json-playback-player-java
Framework for integrate entity object graph operations (field, collections modifications, etc) between backend and frontend.

### Source control, developer and build/ci environment.

#### Github commits (developer environment)
  Signing commits, on shell:
  ```bash
  $ cd /c/git/github.com/hailtondecastro/json-playback-player-hibernate
  $ git config --global commit.gpgsign true
  $ gpg --list-secret-keys --keyid-format LONG
  $ gpg --full-generate-key
    gpg (GnuPG) 2.2.11-unknown; Copyright (C) 2018 Free Software Foundation, Inc.
    This is free software: you are free to change and redistribute it.
    There is NO WARRANTY, to the extent permitted by law.
    
    Please select what kind of key you want:
       (1) RSA and RSA (default)
       (2) DSA and Elgamal
       (3) DSA (sign only)
       (4) RSA (sign only)
    Your selection?
    RSA keys may be between 1024 and 4096 bits long.
    What keysize do you want? (2048) 4096
    Requested keysize is 4096 bits
    Please specify how long the key should be valid.
             0 = key does not expire
          <n>  = key expires in n days
          <n>w = key expires in n weeks
          <n>m = key expires in n months
          <n>y = key expires in n years
    Key is valid for? (0)
    Key does not expire at all
    Is this correct? (y/N) Y
    
    GnuPG needs to construct a user ID to identify your key.
    
    Real name: hailtondecastro
    Email address: hailtondecastro@gmail.com
    Comment: ...
    You selected this USER-ID:
        "hailtondecastro (...) <hailtondecastro@gmail.com>"
    
    Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit? O
    We need to generate a lot of random bytes. It is a good idea to perform
    some other action (type on the keyboard, move the mouse, utilize the
    disks) during the prime generation; this gives the random number
    generator a better chance to gain enough entropy.
    We need to generate a lot of random bytes. It is a good idea to perform
    some other action (type on the keyboard, move the mouse, utilize the
    disks) during the prime generation; this gives the random number
    generator a better chance to gain enough entropy.
    gpg: key 7B34C45B030EAE3F marked as ultimately trusted
    gpg: directory '/c/Users/63315947368/.gnupg/openpgp-revocs.d' created
    gpg: revocation certificate stored as '/c/Users/63315947368/.gnupg/openpgp-revocs.d/2AAD7BC5340A5AD35E0EB1117B34C45B030EAE3F.rev'
    public and secret key created and signed.
    
    pub   rsa4096 2019-07-08 [SC]
          2AAD7BC5340A5AD35E0EB1117B34C45B030EAE3F
    uid                      hailtondecastro (...) <hailtondecastro@gmail.com>
  $ git config --global user.signingkey 2AAD7BC5340A5AD35E0EB1117B34C45B030EAE3F
  $ # Show GPG key
  $ gpg --armor --export 7B34C45B030EAE3F
  $ gpg --armor --export 2AAD7BC5340A5AD35E0EB1117B34C45B030EAE3F > /C/git/github.com/hailton-github.com.gpg.pub 
  $ gpg --armor --export-secret-key 2AAD7BC5340A5AD35E0EB1117B34C45B030EAE3F > /C/git/github.com/hailton-github.com.gpg
  $ # do it for every git clone:
  $ git config --local user.name hailtondecastro
  $ git config --local user.email hailtondecastro@gmail.com
  $ # between every commit and push:
  $ git commit --amend --reset-author --no-edit -S
  $ # or, for all non pushed commit's
  $ git rebase --exec 'git commit --amend --no-edit -n --no-verify -S'
  $ # Run this and submit the content to [Add new GPG keys](https://github.com/settings/gpg/new):
  $ gpg --armor --export 2AAD7BC5340A5AD35E0EB1117B34C45B030EAE3F
  $ # BEGIN: if using eclipse + windows
  $ export GNUPGHOME="$APPDATA/gnupg"
  $ export GNUPGHOME=$( echo "$GNUPGHOME" | sed -e 's/\\/\//g' )
  $ export GNUPGHOME=$( echo "$GNUPGHOME" | sed -e 's/^\([A-Z]\):/\/\1/g' )
  $ echo "$GNUPGHOME"
  $ gpg --list-secret-keys --keyid-format LONG
  $ # If get error below delete gnupg_spawn_agent_sentinel.lock:
  $ #   gpg: invalid size of lockfile '/C/Users/63315947368/AppData/Roaming/gnupg/gnupg_spawn_agent_sentinel.lock'
  $ #   gpg: cannot read lockfile
  $ #   $ rm $GNUPGHOME/gnupg_spawn_agent_sentinel.lock
  $ gpg --import /C/git/github.com/hailton-github.com.gpg.pub
  $ # If get error below delete pubring.kbx.lock:
  $ #   gpg: invalid size of lockfile '/C/Users/63315947368/AppData/Roaming/gnupg/pubring.kbx.lock'
  $ #   gpg: cannot read lockfile
  $ #   $ rm $GNUPGHOME/pubring.kbx.lock
  $ gpg --import --allow-secret-key-import /C/git/github.com/hailton-github.com.gpg
  $ # END: if using eclipse + windows
  ```
  Skipping a build on travis: put [skip travis] on comment. ref.: [Customizing the Build - Travis CI](https://docs.travis-ci.com/user/customizing-the-build/#skipping-a-build).  
  Maven Deploy on central.sonatype.org with travis: Override the "deploy" tag and push it.

#### Claim Your Namespace on the Central Repository (central.sonatype.org)
  Acording to [OSSRH Guide](https://central.sonatype.org/pages/ossrh-guide.html#create-a-ticket-with-sonatype):  
  1. [Create your JIRA account](https://issues.sonatype.org/secure/Signup!default.jspa)
  2. [Create a New Project ticket](https://issues.sonatype.org/secure/CreateIssue.jspa?issuetype=21&pid=10134) with:
      - Group Id: io.github.hailtondecastro
      - Project URL: https://github.com/hailtondecastro/json-playback-player-hibernate
      - SCM url: https://github.com/hailtondecastro/json-playback-player-hibernate.git
      - Username(s): hailtondecastro
      - Already Synced to Central: No
  3. Wait for instructions like this:  
    ["Please provide proof that you own/control that Github username/organization. You can do this by creating a public repo under hailtondecastro.github.io called OSSRH-50112 and commenting with the URL when you're done so we can verify."](https://issues.sonatype.org/browse/OSSRH-50112?focusedCommentId=751790&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-751790)
  4. Wait!
  
#### Working with PGP Signatures (central.sonatype.org)
  On shell:
  ```bash
  $ cd /c/git/github.com/hailtondecastro/json-playback-player-hibernate
  $ # copy the PASSPHRASE generated below
  $ gpg --gen-random --armor 1 60
  $ gpg --full-generate-key
    gpg (GnuPG) 2.2.11-unknown; Copyright (C) 2018 Free Software Foundation, Inc.
    This is free software: you are free to change and redistribute it.
    There is NO WARRANTY, to the extent permitted by law.
    
    Please select what kind of key you want:
       (1) RSA and RSA (default)
       (2) DSA and Elgamal
       (3) DSA (sign only)
       (4) RSA (sign only)
    Your selection? 1
    RSA keys may be between 1024 and 4096 bits long.
    What keysize do you want? (2048) 4096
    Requested keysize is 4096 bits
    Please specify how long the key should be valid.
             0 = key does not expire
          <n>  = key expires in n days
          <n>w = key expires in n weeks
          <n>m = key expires in n months
          <n>y = key expires in n years
    Key is valid for? (0) 100
    Key expires at qui,  at 28/03/2021 08:05:41 Hora oficial do Brasil
    Is this correct? (y/N) y
    
    GnuPG needs to construct a user ID to identify your key.
    
    Real name: hailtondecastro
    Email address: hailtondecastro@gmail.com
    Comment: gpg for travis-ci.com
    You selected this USER-ID:
        "hailtondecastro (gpg for travis-ci.com) <hailtondecastro@gmail.com>"
    
    Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit? O
    We need to generate a lot of random bytes. It is a good idea to perform
    some other action (type on the keyboard, move the mouse, utilize the
    disks) during the prime generation; this gives the random number
    generator a better chance to gain enough entropy.
    We need to generate a lot of random bytes. It is a good idea to perform
    some other action (type on the keyboard, move the mouse, utilize the
    disks) during the prime generation; this gives the random number
    generator a better chance to gain enough entropy.
    gpg: key EA7BBD7233EA9213 marked as ultimately trusted
    gpg: directory 'C:/Users/63315947368/AppData/Roaming/gnupg/openpgp-revocs.d' created
    gpg: revocation certificate stored as 'C:/Users/63315947368/AppData/Roaming/gnupg/openpgp-revocs.d\047DC3C2E28CCC3B76906AEFEA7BBD7233EA9213.rev'
    
    pub   rsa4096 2020-12-18 [SC] [expires: 2021-03-28]
          047DC3C2E28CCC3B76906AEFEA7BBD7233EA9213
    uid                      hailtondecastro (gpg for travis-ci.com) <hailtondecastro@gmail.com>
    sub   rsa4096 2020-12-18 [E] [expires: 2021-03-28]
  $ gpg --export-secret-keys 047DC3C2E28CCC3B76906AEFEA7BBD7233EA9213 > travis.gpg
  $ gpg --armor --export     047DC3C2E28CCC3B76906AEFEA7BBD7233EA9213 > travis.gpg.pub
  $ gpg --keyserver http://keys.openpgp.org --send-keys 047DC3C2E28CCC3B76906AEFEA7BBD7233EA9213 
  ```    
  Update pom.xml:
  ```xml
					<plugin>
						<groupId>org.apache.maven.plugins</groupId>
						<artifactId>maven-gpg-plugin</artifactId>
						<version>1.6</version>
						<executions>
							<execution>
								<id>sign-artifacts</id>
								<phase>verify</phase>
								<goals>
									<goal>sign</goal>
								</goals>
								<configuration>
									<keyname>047DC3C2E28CCC3B76906AEFEA7BBD7233EA9213</keyname>
								</configuration>
							</execution>
						</executions>
					</plugin>
  ```  
  Commit and push 'travis.gpg' and 'travis.gpg.pub'.  
  References:
  - [Requirements](https://central.sonatype.org/pages/requirements.html)
  - [Releasing the Deployment](https://central.sonatype.org/pages/working-with-pgp-signatures.html#distributing-your-public-key)

#### travis-ci.com
  On [hailtondecastro/json-playback-player-hibernate - Travis CI settings](https://travis-ci.com/hailtondecastro/json-playback-player-hibernate/settings):
  1. Secret variables (DISPLAY VALUE IN BUILD LOG "off" and remember escape special character):
      - GPG_PASSPHRASE: Passphare used for [travis.gpg](#working-with-pgp-signatures-central-sonatype-org);
      - SONATYPE_USER: Generated user on [https://oss.sonatype.org -> Profile -> User Token](https://oss.sonatype.org/#profile;User%20Token);
      - SONATYPE_PASSWORD: Generated password on [https://oss.sonatype.org -> Profile -> User Token](https://oss.sonatype.org/#profile;User%20Token).
      
  Attemption: If you are using character other then [a-z0-1] then replace by regular expression '([^a-z0-9])' by '\\$1', with no quote.