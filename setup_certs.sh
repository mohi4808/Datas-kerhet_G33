# rm -f *keystore *truststore *.pem *.csr *.srl

# Create a private key that is used to confirm that our CA
# thinks this domain is trustworthy. 
# Self-sign certificate basically saying "this key is the thing we trust"
openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout ca_key.pem -out ca_cert.pem -subj "/CN=CA"

# QUESTION A: creates a unique serial number for the certificate it is signing. 
# Y: this is book-keeping for version control tracking.

# basically, this computer (which we call a client) manually imports the 
# truststore containing the CA public key which is used to verify that any server
# this client connects is trusted by the CA (by checking the certificate presented by 
# the server against the public key).
keytool -importcert -trustcacerts -file ca_cert.pem -alias myCA -keystore clienttruststore -storepass password -noprompt

# create a keystore private and public key used to prove to others that this 
# client is trustworthy. 
keytool -genkeypair -alias clientKey -keyalg RSA -keysize 2048 -dname "CN=Eric Enström (er7383en-s)/Mohamad Alexander Hiadan (mo4808hi-s)/Carmen Wretblad (jo7445wr-s)/Filip af Klinteberg (fi7584af-s)" -keystore clientkeystore -storepass password -keypass password

# generate a client.csr file that contains clients public key and the common name (CN) 
# csr = certificate signing request. The csr will be sent to the CA later.
keytool -certreq -alias clientKey -file client.csr -keystore clientkeystore -storepass password

# manually (client and CA are still on the same computer) the CA signs the client.csr by 
# accessing the file.
openssl x509 -req -days 365 -in client.csr -CA ca_cert.pem -CAkey ca_key.pem -CAcreateserial -out client_signed.pem

# import the CA certificate into the client keystore, essentailly the "proof of trustworthiness"
# for the CA according to the CA. Used by keytool to confirm that CA has signed client.csr request.
keytool -importcert -alias myCA -file ca_cert.pem -keystore clientkeystore -storepass password -noprompt

# place the client_signed.pem (which is signed by CA) and place it in clientkeystore
# The ca_cert serves as the root of the cert chain, proving trustworthiness.
keytool -importcert -alias clientKey -file client_signed.pem -keystore clientkeystore -storepass password

# verify that there is a certification chain. 
keytool -list -v -keystore clientkeystore -storepass password

# ANSWER QUESTION B AND C.
# QUESTION B:
# use the -ext extenstion to make it explicit (step 4)
# use the -extfile extension to makie it explicit (step 5)

# QUESTION C:
# extra information (fields) in the certificate providing additional instruction/information
# around the certificate. Otherwise a certificate is just a link with a name and public key



# 1. Create the server keypair
keytool -genkeypair -alias serverKey -keyalg RSA -keysize 2048  -dname "CN=MyServer" -keystore serverkeystore -storepass password -keypass password

# 2. Generate a CSR for the server
keytool -certreq -alias serverKey -file server.csr -keystore serverkeystore -storepass password

# 3. Sign the server's CSR with your CA (using OpenSSL)
openssl x509 -req -days 365 -in server.csr  -CA ca_cert.pem -CAkey ca_key.pem -CAcreateserial  -out server_signed.pem

# 4. Import the CA certificate into the server keystore
keytool -importcert -alias myCA -file ca_cert.pem  -keystore serverkeystore -storepass password -noprompt

# 5. Import the signed server certificate to complete the chain
keytool -importcert -alias serverKey -file server_signed.pem -keystore serverkeystore -storepass password



# create server-side client trust store
keytool -importcert -trustcacerts -file ca_cert.pem -alias myCA -keystore servertruststore -storepass password -noprompt

# QUESTION D: yes it is possible to just copy the trust store since the trust store 
# just contains public keys which are already available. 

# QUESTION E: 
# the trust stores are encrypted (which the respective passwords decrypt) 
# such that an attacker can not alter them to include domains/servers/users that 
# aren't actually certified by the trust store
# 
# The storepass in the keystore serves the same purpose as the password for truststore,
# i.e no tampering by attackers. The key pass for the keystore is an extra layer of security for
# the private key of the client even if the store pass is bypassed. 
# This is because the private key is so important. 

# QUESTION F: the server resends the message but backwards.

# QUESTION G: enables mutualTLS (mTLS), i.e that clients must authenticate themselves to the 
# server the same way the server authenticates itself to the client. i.e the client must
# present a certificate by a CA in the servers trust store.









