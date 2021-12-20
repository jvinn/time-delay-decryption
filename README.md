# time-delay-decryption
- Simple text encryption except users specify an amount of time it will take to encrypt and decrypt the text
- This means that even the user that encrypted the text must wait for however long they specified for it to be decrypted
- The key is derived using PBKDF2 which makes it impossible to avoid the decryption time delay unless it is run on a more powerful computer.
