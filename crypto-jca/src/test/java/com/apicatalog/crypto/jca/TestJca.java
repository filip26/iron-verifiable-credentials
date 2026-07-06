package com.apicatalog.crypto.jca;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.crypto.AsymmetricSigner;
import com.apicatalog.crypto.AsymmetricVerifier;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multicodec.codec.KeyCodec;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestJca {

    static final MultibaseDecoder MULTIBASE = MultibaseDecoder.getInstance();
    static final MulticodecDecoder MULTICODEC = MulticodecDecoder.getInstance(
            KeyCodec.P256_PUBLIC_KEY,
            KeyCodec.P256_PRIVATE_KEY,
            KeyCodec.P384_PUBLIC_KEY,
            KeyCodec.P384_PRIVATE_KEY,
            KeyCodec.ED25519_PUBLIC_KEY,
            KeyCodec.ED25519_PRIVATE_KEY,
            KeyCodec.MLDSA_44_PUBLIC_KEY,
            KeyCodec.MLDSA_44_PRIVATE_KEY);

    static final Map<String, AsymmetricVerifier> VERIFIERS = Map.of(
            "P-256", JcaECDSAVerifier.getP256Instance()::verify,
            "P-384", JcaECDSAVerifier.getP384Instance()::verify,
            "Ed25519", JcaEd25519Verifier.getInstance()::verify,
            "ML-DSA-44", JcaMLDSAVerifier.get44Instance()::verify);

    @ParameterizedTest
    @MethodSource("vectors")
    @Order(1)
    void testVerify(String algo, String publicKey, String privateKey, String data, String signature) throws Throwable {

        var verifier = VERIFIERS.get(algo);
        assertNotNull(verifier);

        var verified = verifier.verify(
                MULTICODEC.decode(
                        MULTIBASE.decode(publicKey)),
                MULTIBASE.decode(data),
                MULTIBASE.decode(signature));

        assertTrue(verified);
    }

    @ParameterizedTest
    @MethodSource("signVectors")
    @Order(2)
    void testSign(String algo, String publicKey, String privateKey, String data, String signature) throws Throwable {

        var signer = getDeterministicSigner(
                algo,
                MULTICODEC.decode(
                        MULTIBASE.decode(privateKey)));

        assertNotNull(signer);

        var result = signer.sign(MULTIBASE.decode(data));

        assertArrayEquals(MULTIBASE.decode(signature), result);
    }

    @ParameterizedTest
    @MethodSource("roundTripVectors")
    @Order(3)
    void testRoundTrip(String algo, String publicKey, String privateKey, String data, String signature)
            throws Throwable {

        var signer = getSigner(
                algo,
                MULTICODEC.decode(
                        MULTIBASE.decode(privateKey)));
        assertNotNull(signer);

        var signatureBytes = signer.sign(MULTIBASE.decode(data));
        assertNotNull(signatureBytes);

        assertFalse(Arrays.equals(MULTIBASE.decode(signature), signatureBytes));

        var verifier = VERIFIERS.get(algo);
        assertNotNull(verifier);

        var verified = verifier.verify(
                MULTICODEC.decode(
                        MULTIBASE.decode(publicKey)),
                MULTIBASE.decode(data),
                signatureBytes);

        assertTrue(verified);
    }

    static AsymmetricSigner getDeterministicSigner(String algo, byte[] privateKey) throws Throwable {
        return switch (algo) {
        case "Ed25519" -> JcaEd25519Signer.newInstance(privateKey)::sign;
        default -> throw new IllegalArgumentException("Unsupported algorithm " + algo);
        };
    }

    static AsymmetricSigner getSigner(String algo, byte[] privateKey) throws Throwable {

        var random = SecureRandom.getInstanceStrong();

        return switch (algo) {
        case "P-256" -> JcaECDSASigner.newP256Instance(privateKey)::sign;
        case "P-384" -> JcaECDSASigner.newP384Instance(privateKey)::sign;
        case "ML-DSA-44" -> JcaMLDSASigner.new44Instance(privateKey, random)::sign;
        default -> throw new IllegalArgumentException("Unsupported algorithm " + algo);
        };
    }

    static final Stream<Arguments> signVectors() {
        return vectors()
                .filter(a -> "Ed25519".equals(a.get()[0]));
    }

    static final Stream<Arguments> roundTripVectors() {
        return vectors()
                .filter(a -> !"Ed25519".equals(a.get()[0]));
    }

    static final Stream<Arguments> vectors() {
        return Stream.of(
                Arguments.of(
                        "P-256",
                        "zDnaepBuvsQ8cpsWrVKw8fbpGpvPeNSjVPTWoq6cRqaYzBKVP",
                        "z42twTcNeSYcnqg1FLuSFs2bsGH3ZqbRHFmvS9XMsYhjxvHN",
                        "f3a8a522f689025727fb9d1f0fa99a618da023e8494ac74f51015d009d35abc2e517744132ae165a5349155bef0bb0cf2258fff99dfe1dbd914b938d775a36017",
                        "f1cb4290918ffb04a55ff7ae1e55e316a9990fda8eec67325eac7fcbf2ddf9dd2b06716a657e72b284c9604df3a172ecbf06a1a475b49ac807b1d9162df855636"),
                Arguments.of(
                        "P-256",
                        "zDnaepBuvsQ8cpsWrVKw8fbpGpvPeNSjVPTWoq6cRqaYzBKVP",
                        "z42twTcNeSYcnqg1FLuSFs2bsGH3ZqbRHFmvS9XMsYhjxvHN",
                        "f3a8a522f689025727fb9d1f0fa99a618da023e8494ac74f51015d009d35abc2e03f59e5b04ab575b1172cb684f22eede72f0e9033e0b5c67d0e2506768d6ce11",
                        "fc6798ff29f725dfd39aa4daf60fbb423cf9baf4e157f6b49f112c201015c6e730dc877154e65cf467f8ee2b61ec86d98ed78334b1cc9f3dba2e1745f37205e92"),
                Arguments.of(
                        "P-256",
                        "zDnaepBuvsQ8cpsWrVKw8fbpGpvPeNSjVPTWoq6cRqaYzBKVP",
                        "z42twTcNeSYcnqg1FLuSFs2bsGH3ZqbRHFmvS9XMsYhjxvHN",
                        "ffe5799489119c7fe3c528715e72bd39d2ec6b4ab345978df32e9a9312648ec2559b7cb6251b8991add1ce0bc83107e3db9dbbab5bd2c28f687db1a03abc92f19",
                        "ff15c3b599eb9b3cad05df9d8e8b39a70a86375833b53743c764ac0a88c4457d60707fd7d073e03d906130631d87803f80a9824dc9939632ba92d418181be9d16"),

                Arguments.of(
                        "P-384",
                        "z82LkuBieyGShVBhvtE2zoiD6Kma4tJGFtkAhxR5pfkp5QPw4LutoYWhvQCnGjdVn14kujQ",
                        "z2fanyY7zgwNpZGxX5fXXibvScNaUWNprHU9dKx7qpVj7mws9J8LLt4mDB5TyH2GLHWkUc",
                        "fe32805a26492eac777aa7a138f6d8da3c74e0c7be7b296dcaccf97420c3b92eaad7be6449ca565e165031567f5c7cbc11033878f36ffb458c6495fec9c8814dad5215aad131041e6667db28fef6ea718d0de0eb4546bf527746ad2bc908a4320",
                        "fa5999d1154a3fb5db8805fa762c8c41c1b7f40a231a5d42460d36245349771835f43fe0005295d2061be1789589c1f6385312f0e2e36709c310c77e8289587b79b29ecf7aad14ef61a1393cc2e1f93a7a354bd76bab47d558df060c6ae218975"),
                Arguments.of(
                        "P-384",
                        "z82LkuBieyGShVBhvtE2zoiD6Kma4tJGFtkAhxR5pfkp5QPw4LutoYWhvQCnGjdVn14kujQ",
                        "z2fanyY7zgwNpZGxX5fXXibvScNaUWNprHU9dKx7qpVj7mws9J8LLt4mDB5TyH2GLHWkUc",
                        "f83e5057817abb0c6872eafeaba1a9e53893c58eeb7414fb6d8aa3fa8c7917f7ad4792890b257c598baa17f4fbe6d183c3e0be671cc1881035d463158c80921973dab3534d4f8dfacf4ff2725a4115eb718e49d66de0e90e7365cd6062abf2259",
                        "f8b7462ce62db0c8ff19878c4b3561c49eb71b4a743086b6d5b0eda70ecf0afc5a03fd88eb207d66b262ed87fd200a4e8e62716e0b329c032b67726b4b0fc737a44c1cefdba2fdccb3ece74cc5845aaa93374455a726f6ee4f5f30da9427f608a"),
                Arguments.of(
                        "P-384",
                        "z82LkuBieyGShVBhvtE2zoiD6Kma4tJGFtkAhxR5pfkp5QPw4LutoYWhvQCnGjdVn14kujQ",
                        "z2fanyY7zgwNpZGxX5fXXibvScNaUWNprHU9dKx7qpVj7mws9J8LLt4mDB5TyH2GLHWkUc",
                        "f83e5057817abb0c6872eafeaba1a9e53893c58eeb7414fb6d8aa3fa8c7917f7ad4792890b257c598baa17f4fbe6d183c3e0be671cc1881035d463158c80921973dab3534d4f8dfacf4ff2725a4115eb718e49d66de0e90e7365cd6062abf2259",
                        "zq3EuTeLiGurmB2JR5oL8oWEsT7u2tba4HT1oZbiMYWc5qzsoW2kLYcBcF4HM5vCpJyTkceULKrVXuJQkXeN5seL4uXrFNFRMm53GWy1Yrto8rTWxZi9DkNeWP7yUPs7ELAm"),
                Arguments.of(
                        "Ed25519",
                        "z6MkrJVnaZkeFzdQyMZu1cgjg7k1pZZ6pvBQ7XJPt4swbTQ2",
                        "z3u2en7t5LR2WtQH5PfFqMqwVHBeXouLzo6haApm8XHqvjxq",
                        "fbea7b7acfbad0126b135104024a5f1733e705108f42d59668b05c0c50004c6b0517744132ae165a5349155bef0bb0cf2258fff99dfe1dbd914b938d775a36017",
                        "z2YwC8z3ap7yx1nZYCg4L3j3ApHsF8kgPdSb5xoS1VR7vPG3F561B52hYnQF9iseabecm3ijx4K1FBTQsCZahKZme"),
                Arguments.of(
                        "Ed25519",
                        "z6MkrJVnaZkeFzdQyMZu1cgjg7k1pZZ6pvBQ7XJPt4swbTQ2",
                        "z3u2en7t5LR2WtQH5PfFqMqwVHBeXouLzo6haApm8XHqvjxq",
                        "fbea7b7acfbad0126b135104024a5f1733e705108f42d59668b05c0c50004c6b003f59e5b04ab575b1172cb684f22eede72f0e9033e0b5c67d0e2506768d6ce11",
                        "zeuuS9pi2ZR8Q41bFFJKS9weSWkwa7pRcxHTHzxjDEHtVSZp3D9Rm3JdzT82EQpmXMb9wvfFJLuDPeSXZaRX1q1c"),
                Arguments.of(
                        "Ed25519",
                        "z6MkrJVnaZkeFzdQyMZu1cgjg7k1pZZ6pvBQ7XJPt4swbTQ2",
                        "z3u2en7t5LR2WtQH5PfFqMqwVHBeXouLzo6haApm8XHqvjxq",
                        "f66ab154f5c2890a140cb8388a22a160454f80575f6eae09e5a097cabe539a1db59b7cb6251b8991add1ce0bc83107e3db9dbbab5bd2c28f687db1a03abc92f19",
                        "z2HnFSSPPBzR36zdDgK8PbEHeXbR56YF24jwMpt3R1eHXQzJDMWS93FCzpvJpwTWd3GAVFuUfjoJdcnTMuVor51aX"),
                Arguments.of(
                        "Ed25519",
                        "z6MkrJVnaZkeFzdQyMZu1cgjg7k1pZZ6pvBQ7XJPt4swbTQ2",
                        "z3u2en7t5LR2WtQH5PfFqMqwVHBeXouLzo6haApm8XHqvjxq",
                        "f04e14bcf5727cba0c0aa04a04d22a56fef915d5f8f7756bb92ae67cb1d0c4847517744132ae165a5349155bef0bb0cf2258fff99dfe1dbd914b938d775a36017",
                        "z57Mm1vboMtZiCyJ4aReZsv8co4Re64Y8GEjL1ZARzMbXZgkARFLqFs1P345NpPGG2hgCrS4nNdvJhpwnrNyG3kEF"),
                Arguments.of(
                        "ML-DSA-44",
                        "ukCRKDtY8Do_dXzYGyuX7BY-1dDYM4FuSiw0gFdO-eJXFH0eqlt4_CP4sEISGAzNlKDzLpUJWoInRywXOpd7FCp_QAJAlL7iRo4cepKhzhlq8xt6qd5jkhYF9tNH8z3RGDl9aunNy_06fWLYNScWd5RmGg46Po8T-kIjMMkJftaqqZcGDxktpu9Et2bnaZMx4K98YyG1urUpM9lgvldgg2qv-6XCrm2uXlJ9U-HN4xtQKn4Ug-5xPwbhPGR2pbcBScTFotkhBqLc2eQLL6zPutWF83sSZbOhD_11BjMkeiLyJbMeHCIhz5GDIbPksEFIaSho3MdFo5fpQ8QZoqCit3Jn4ddfuShfIoLU1Hw5EZ0xBiqOU7e-TINd7-7HsgHLmYMGnpqljm1ot3c3cfalYsg87WQuSscO7XNH3Ewa-cgU6Bnj1SGn0plTy6Yq-GxU8XUBPwsK_IoIJbXWC0UD97c9UYpNiZi0-ECnbB-Y5_SM8auMfoIeap_buAOdXlJZmcp8xNCXI59AN9-96Sdhks5L-JmsELzyjAgjqNx8Zt3KPFc2jSwNjDVC1fEa8FdDdDT3WNkF6KTt65lb5_aIkFh20nOvT7kIJcKTgmhRGNJZgGSPYVbMypaQoaac8dtEoQjvYgnO-rM_RcsiWMHNc29br3o5wdiLXdr63MoX1lEWu_THBfeP1JuxrSbUmHOByepWbubbSM4iVQITCxBHZT0Mj2bWwIxd3nZUajzebyEnsfitV01kpzlO7bzY2uxSzyplTkRfppc_7YH0y0PHaggw0cIXNSh73wVqNZmzmJx5W0_akrvy5oSz9ZB1Io2p_fTxzibefwO700bUqbElV_yuCjD7EJ_Hfqbog80y_g9TK6koX7wYwqFNQxBVavKC-HbcT7yPdvzs9hlC2MNWCT3W7gVgeYr4AFgbV9EgMcH0GtJDKYw8vkpB_vTsaSTGZAj3TNKalAwiGO50VAmF5tknF96kOrWmNL0MdkXhnm1vXgDpP68bMt4r2Qr-hNdJ4s3_nqmSDYTnZRA4qjXjrgKQfO19txt0tX7LifE1GZ1bQyS7NqHWXyMEhw6_F8pc_tS16VhvJO_FM7CX51mLLkLCGl7DsmbnEIsVUW9qlCxb6bj53UyijTYdu6uLZW9JISE2B4EevxzwDu9UGcJPHmJYi1rRQAP__jH97GiQC8FvkdAEfKqcwV9jAbBPQPG6lUkBLcoijgR3Bcwd-ta92oeZmcpoJ97PzzBbCL-NrppJ2HHQ1SMsYWoPveZTmZc66YBA0P9YfT4hZ0RQiP2gxB4snTvMFI0Ot6Q2nQ0p5DMxmWqIaCKW53rqn16AVXQeqC2TJjlbjA9sC6pr8GEGY2OQUgEmWu5GmnOSz1lNY7fNHJypChnieI_hyYiy06qouUpoHA5z_IUtfzZoMIG0yJiGUUpF9BJvYChDECCqaUM1kWnO5tKcohSKq5Hqwu_EWDRYF2tj7igSimZkS4Pts41tu8nIaVk5EkzAX9gCR2EX3Lk869mIxSyBS3MyG_NotPcbm6uXDn_YkV5Z0HkxUxYRA9hIG-UhKhK3VOaHZP8GcQN8noOMa2CnPd208X6HOzlIlxs7SRbzppUs_fHN1eROglNy-2oJWGmo-xOy0Qd44TtY0S_bYhu6iH6inrx3-yncSrWFxEiYosvYJD4ZBSyrV4d6UsfeNSHYS0ODTsdPqz4SYTeloZbIx8XWz7fxLXlNyLr3s9tp-Q25f1vTIrmQL",
                        "ulyZKDtY8Do_dXzYGyuX7BY-1dDYM4FuSiw0gFdO-eJXFHy45NUAoR0EC-oa-BYzQKUDnsmvnB6LnwTrKgLK72LjR0HtnZt33kFZNO5itZ2QP2hbioyfpga_gZzp_WhCRn80lMEvfoT34jtVS4yiW4VnbAZrLI1kNLMoPrll7_-GzZhQBAFOmkIMAjQtCSeKIKdjGhVKACAQpbWS0hUCURUhAMeJEklNIkmQyctFALZsGcQSwcKK4JSGxURuZjAwIigtILZsmamKicASwDCRILgg0IQQTABojKeOEKQQ1BGQSbdymYSCyCAq5TBs5SkIEgMIkicO4USIJkNHGMCApJAkihshAipmEAAuBRdoGKQqxRZCIKOEIhMqgbJpGCAO0JVtGgoMEhRpFASI0ENQUKYg2MkzEAKKWSAPDCZBADAw5BpyGkJQYIhDFTGCIQREyQQIBRcyohIoCSRiiAOOCTUI2ZgoGbiNCRAKmZVswgBm2KQEWaiAxCRukARkDEYAogdKAIJDGTeIIZRumBFTIZFvGMcoyZpkCjOMmgZiEgJy0hNoiAgQ1EtPIMSEnTJnAacBGiVESjKMQbNAEieIYhRglIUgCjluoAAAwCggoAVyCCCEZSkEEimNAjpm0jdkiIBkQIZPIkQiyjduEASO4REIkcAmRQSGRKQCBCFogAVIQLsgoEeNGMiA1KRozRiFJBssobCMniBtJKRilTZK0iMACcpI0iIo4QoMgipAYQKM0TiJIJJNEZhzDSaHEENsCDOAiDYrEMEGCMRQjMhM4SmESiJqkDAJJLcFEQcuWYRI2ikCicAwHCchCkZEgUhg5ARtGaQEyRNy4kAghLlPAkdLEMUwgRtoyJsogTkOCAMmybdiShdiUkMqYjInCaEyyKIQiRBFFKctGRcI4RqE4ShtCbUoQbeGSbNsgadgYLtwSEYQChMSWYdmkMWLALUOELZoSTQrBJVEgLmECQCGoSNC4AAgBKAgYESDBUdA0htNCKcEoahsAAUNGSQCYZFNAYZwyYFkwguIoIiKHMIsGSALGaQIRRkiEbAgykKDAMBBECdw0ikqmUMKSjOEikIs2KhAAKlG4DRhJKgohKYxCMKQGjphCDhrBJQHJMVMiJEAIYSPHYOEQMQLBIFw0EkI4bCQWThOUJJSSBRk0CNlCkuTARCIXRhxAjcECEVjCKaPCZVLIcql4HoeXD1OaOqFehE7e2oAeL97U_fVM2QfuMSUocWXlca1SovfqIY1cVn-HZC5kgjrFfEBw9J7VV9o10GSat_0_ZxlDRZMjZugj6hPxi0SgBTFILoBagT2g3T5oNLyDBkf2K4HfyUzuWGH9xrwU4YtT8IiVrlGsUpahlfFBm3Aykx3xXsCh9CyGqDud6JW3uDYdMHQX80OmiOcqtncQfrSWtjp4HN0TNsJH02sMX3QaIlUwkOdZfA8cFQsTctoRfjPS1yiQxnz8CIkrHHnSXLc3_Tx7WMH0t1n3XkToOfIXheEiKXNQfkSiw9YFUcyu09lNhPxIirxNYtUB3Nol_LmwsbawT1eUiWiAxjMhoa6gig87Z-DLVe_RLivg7Hw-YJswhiRSRbLMwcQf86-ntscG2lpGDP0Tzh_ME5tryC0NafdUwDwN8xBoP3uyPSru7iENwMpXeMtn6qNCLgYWYTZg6G89uU1WHJUPvMP_njwYNqVl_YYnyMxL1vqbwuUkRbXAalI9mGNDLyNpGmyOWSzftFwZEXd6Zr37z1nFQNWu9nMstx74JT5oXAIp6mcPO4t5EoI4q9Ku4kulk8WMvJo4awoq4nPmyNp3IZE6cIYw-SJDhdIoLNg1hfF2NIpIzQOuFNge3cQg3vbKlL9cV1uPOLuQZYhqLOmW4rsxh2qKdPEJvI30y2EGBiEs3-oWv_spYtckLvPk0BYVukp2w4bHZ2DC_qDA5HJkFRroHO029fZTmPmpyIbPry6fKes3MjMxEo922hXyKwooyr-C0MU837eTPx3JZuxxOu4sUO5FP5GQ42RNM43EMj_7M1nfxeZhxuWqzVnPCnngU5XVPqIEjGsG_HiTikz-KUXerygny4HlyJjazPvCiOz_OB-M7t6fYSgoo28CZ2vwelVbEcooTX1UtUW63rejU6xANv765iVgruw6TwAdOpxvDeHcOfhc31QV62X44Pl5-nAXIUczh-i-QtzOtD6o_goZ0DOWv7ovzqi9BEgMIkiTbwRMHCVpnqgqp0bpZg61ZepIzOkwaZGHbzfX1rqpZseeRDZxfheLXf0jb0Ph5vVL4f-e0mWE-0hHm7Ga3g3TUADdrVzykMeOLrceNQ-zsrVfdM4jvbbtO-3-Ip83OI7Trb9SwJyhCj1ybxchP1MNSGxRI5Mct0YDDLQNVulSF1uQyidav8nIk9NTybUwkgMo7SC0oJxiI70gRHspfWWTJAgIGof_yW37nz1HoJEtFuGbM5Q9j4lSDJe6BCCNTqBZ-ZyCigTesm6Gdhc10A9NG72WDbwY23GxxwODXZ7-yCs0fjoVB9yQHhj-9EvtHQvkXUC_eT8OkeF3Ee12_-lYJCgyhLvcrdlVf4XeV3g90ULcEcWyd4AwW3xhfw1ekdvddQ0OhRQlaOgtfIIR77-Px8kAN35fcwGaHJA30j0XfbF2QxggKBn1kZSxmPQGXwpJqDhkp3yB7g_ZaYsAzNpSAb9qUSrvLAie9fqxtZisPhRDFfIWQVSxeOUmK34XfPd0q-1SLeWbHFwP548dGtpzWZtzASLGLyGB4r27takf-44UXhnWmWFHPRToRZtMH8ldVM5OYiAmYlqufclf9Oi9kAtKziLuqkgETRRnlh4yc5OwMKAMxsqUpEeNkvI53CbmVBTm3cJHqvvksJE3MAdiXXmGKxDlG1vuMfM7LuBO2R_f-K168PnQ-DRV9Awf8DJTs7sXITxRuNQ9d6CPhXNTkWvxKJswnemHSR1Hrd6YIKDq0RQ6GKgzInRJFtMjQcRk1_uFQILjtnyd9JiAztD5JNIopTGC7t9SjnFz61oy_1E2E_5jYe6LVzJ7-ENhOiCJeWJTJTQORyi6Qf_IWcQGO03uBomHqCtdq2hcSSuSprohuzFhg3afTjR8a-7skGJ_s3zYODM4G8EkPYh4ZxieG7mvMtwBHzTUBYu_QMpqdiPmDwUh-dacRZDClO-p5IZsR9Dt-s9aMWR27A93NKtdflPUoDuODP6NyuWlqKIfvGdUwklKv9k6OK1cQn_i0CGsJj_RoCIiX-byFUMXnngy4Q07Ad2AbGq0E6TEESh6gpzVKo-ucqc-XkYSesHSbqOpOdaxdRE991tzgt05VpAtf-zRlZIKw0khtzXd3utNqiSNNPOK3nBG6-Jtqr2uWfEHof98G-dSvcyT3uPMHfAn-J-7Tu5q1D-tKRQGnpNsyzST",
                        "ffa074359f280ed66bd74b65c8f644175b2f413e24ef3b917d557b3bc261b28f203f59e5b04ab575b1172cb684f22eede72f0e9033e0b5c67d0e2506768d6ce11",
                        "u_LfmjHGGR1q1D2_Ue-KKAwdZpSirCp8NGDu6EDN3DzRKUAlz_pl_JNlzd3Pg4dNAUCGc-avNt1ZQr4y5P_h6C3tP_HTaNIfVIx1O-D9Ooy59PgjHZLWAWiMiVx2hfPiLg88dVAMqpp1gjuiL8XRYiGLDQONRCoRXjTd-fZ6cg81p-NZ8ISINOjWJfWkf62avqWVp0yTBM2jPaNIkbfHCoUpwdwSj_NhXJMd10qag_hF_Hob_Abjuz-eAmg76AkSP0R-8l7peBmqfwP7TcSKPGBT_FUGjwbEkse2136YiNdYlk-NALtyz1PRk85tXood34_RTqCiHpa465ey2cJQ7Ve8zYQvU00u1SLF49ahIZfFGbiXpbVWHHCmvIGuqIxwq6hkRT_f-5T8SHOWmRUutnk7l5bvK2BqSBB2vd2yJexEe1j_wH-Nd5TYmqFq5Zg42zxpd7abSPqcNkkTZUPeOsrghrFT-glm0xr2uWAoTGn6FYye-_YM58hV2JE295a2PPg1wJcvN1AEnhwANGL_SlM6r2r0g5wesPY0cBMNJpXenI4_iY5EjINGZcjlShAGFGqx37hyPElHaYUortI_5s-PwyWGSJeHJ8IJBNO_M0okoy1yF6DefSbdDghWOnRWUYvzJMiwvM8TkxSABlIJ5oevyiZR5cHVpKgvaNYsIxzkK2bUVPJ_E_GpIHEpGnlS5Y3yji_SdoG5mufkj3D_PLY71qzs3k8SfN7KRtXp5nwwjw1khyHpqN-sRbV1gu31-NcWVR35x_a8zzxEcV5i1Q6hzpp9Gi_VgfOxJZIeuOc8G3uDEmjnIlhFfHgnCIwRJ2TQZs90nahcrYyNUdy7PMtmRKBiVIBYC57fA3kndbzHfzM2Ed12Mv75MnSK1Io55nfk8jTdxKCynrEZRtGhxLlQ9eoA1zO66haknhc95FVJZh5e0tjew4EQprlhdpBR4t0fJChAAqgrJNQ9ov17Nt5Ly6N8gGzgImzI1qN2mClYHX7ow4yzAeetZW_5WEaLyn57f0Tf1kcz9AS3TJNEOHgfAs7ZXfDNCFLgR5Bb0XWhL_2IP6rbeChcB9WQEQX36kGjIJfnUx0B2OSGNUF0Vjj3Zkr6IP37MATawid9pbYtytcY5KC9pn71KhzBqA5c6xJ4mdFXDZIbA9qEPfDsUao4GYPoVeV-0YXVXe7nlUmFp4WTCxoePGZbJcRWtHTegpVgtKvynhmIdHa_CYP_M_KKaeAhiufFCc7JYmyj6rdwz-xipAiAIHsdgrgtRgEdH_jrhFKEPP7gC9S8tK6NCpEWpzXaRnBkQTAQyt0IMMyyGHAgoHx_3wFQynT-7IeKb5si_i7penySiP7QQigJ6SB1nC7m0aLDWvcZ-X3A03BMqlYTgE8EWvJHtjmsjJSoOulD_WN_mLhXIIkI8mhX6-1GggQBg3JkqyRr1i-hdnpjE7iCgQnaS6Gk8O0gU8i_EoQh97275vaf-NwaXun-p3X_0U3cwTzsGGD-EIJu-zUwSThIUa4nKyImm-yrWh-r8tnQ4_o4gl8Avsw4Y4YQ8v9d3U0nr3E_xw0vKWMLxenn2xYGxo-wy-7W-9S8-SnjqyUxeDI1IwRzHYWHjP6_NK88jzpVTP90ySiIg_pAPxff7-xU-xtIgc_41bH1mjIVL_lK0sTS9F6acxGY7J0ijPMglCD72QUUx-J0OdESpxOhx3o4jc2thRhYzLPmZ9mXEMHwMf87Td-pdwBFFI8ff4eOozn7nZbih27UL-w2Xv4fHbBkAi0njje7JDkl5DyuyCkR86aVv1yTFemiq3rY8oHhCUIiUq3HyqCA5wD0mx43RDH00bTEQb2hPQ6CEKrgzpSp39fS6hsZBGCFXkPxsQAGOkRlnTPkIdg-pl1sVz4CNcpDcOBZ9ZNFBSSw7Nc4DUefLTD2n6nCswhQJL2wBbPJt24M5ckBD_0_CH7WwprAJKHgrJS1NY7X5iDaBl_8GvIW73xKsYBL9ZdUbuvp6ALAuQD9lsrXlzGZ0VbO7ap4484dr_v4zxteh82ksGbZZ3ujCqmIU9lqTtR5ahX9GGsLfIqczWyZbczYS9xU3ow7rG5muPDuZ9EVScvEkTbnP8Dw2238YKTuZIIALDShMYUqwPjnUpORlLg_MyRodiaVR6PdXbYSAeRchLXPNiwifsjvHbLnTeFeVGjshluYEpB9FdxJRAJpPMTsa0-t06q0e4FZXU9fs9lOwz0HpbJEa774XUTrM1XtqyAh2yKHhtTCQ6_PduO9PJE-OkLx12W2nc_ql7IW7BGyylwee-RaSVKmg26G_0ozVx--pBa-HZpcgNIleI3CY1TwMzZsT49QNMJ0hmsnr5mumiX8eRmmo_GeT6fKY3AxSnc0Y6yrTmT5MP0wSz3jssXplN6UkzahDPUww_zN2Tc10Kf9ehHrqCGrZho9r5Eq1o6_yXgdNogbiEl_VltIVsujdwoUHNSOzZJ2lKYsB2X0o6G6nSvXjWJr9F3DBivxsDERQTWOyGB3t_0ogmCGnwelnB9k-mz90TwbQ_2K2xd7Eoe6rfSJDTDt25u9dSI9TGvDow7fmglua7obkDlXz8JzQCfNhPqAO2Mv9TqqGywbDqwucJDgfFRhf6Rp7LTRUHQmQeLZCu1wfCCkBHs2SEIyj53AvNP1qtoLon60AQ5jP0sW1XTZHsi6xS0W_6qZq5UXFHNoERFOdkb1Ry1F4ScixWxHvXMn4SkRfQMga7ejtK2AO2ofJc-oun6IkE_qiNnu35JXJjvANMhgPwmoHme75e_bFvmHQn0JY2Fg1DrrvXWgE1S2uDJwtXJ3pi8SlRUfzEJR634Zw4jZWxxSYc96J1PO45jpA67l2PDbIxrqKZhkx17zy1ASfdSnlRLYcbqaTfPqPb1IuXm31cA56IQijOM8YNFbEfe9u5ysx4q-QyiluT91ZcCMtDOyR8pXUBR8UTSFkMNVrvE7erdosWSZM22SHF7LYlSK4-L9pybOndjM81gE_2yRtWgNkmEhTP9wGqBa_MfDxSa7iTUBr9joSn6SgCUyfufT1ZRlnIF0FU_qYYWWL9jdbV4k9v6aetHY5sgS-2fgJRGvy8bKD6M55TfMxQrI3XGCf6RAZGlNnf47T6g0SFx8hJi1EVFljcIuQo7PGBoS7vfUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAUOHyQ"),
                Arguments.of(
                        "ML-DSA-44",
                        "ukCRKDtY8Do_dXzYGyuX7BY-1dDYM4FuSiw0gFdO-eJXFH0eqlt4_CP4sEISGAzNlKDzLpUJWoInRywXOpd7FCp_QAJAlL7iRo4cepKhzhlq8xt6qd5jkhYF9tNH8z3RGDl9aunNy_06fWLYNScWd5RmGg46Po8T-kIjMMkJftaqqZcGDxktpu9Et2bnaZMx4K98YyG1urUpM9lgvldgg2qv-6XCrm2uXlJ9U-HN4xtQKn4Ug-5xPwbhPGR2pbcBScTFotkhBqLc2eQLL6zPutWF83sSZbOhD_11BjMkeiLyJbMeHCIhz5GDIbPksEFIaSho3MdFo5fpQ8QZoqCit3Jn4ddfuShfIoLU1Hw5EZ0xBiqOU7e-TINd7-7HsgHLmYMGnpqljm1ot3c3cfalYsg87WQuSscO7XNH3Ewa-cgU6Bnj1SGn0plTy6Yq-GxU8XUBPwsK_IoIJbXWC0UD97c9UYpNiZi0-ECnbB-Y5_SM8auMfoIeap_buAOdXlJZmcp8xNCXI59AN9-96Sdhks5L-JmsELzyjAgjqNx8Zt3KPFc2jSwNjDVC1fEa8FdDdDT3WNkF6KTt65lb5_aIkFh20nOvT7kIJcKTgmhRGNJZgGSPYVbMypaQoaac8dtEoQjvYgnO-rM_RcsiWMHNc29br3o5wdiLXdr63MoX1lEWu_THBfeP1JuxrSbUmHOByepWbubbSM4iVQITCxBHZT0Mj2bWwIxd3nZUajzebyEnsfitV01kpzlO7bzY2uxSzyplTkRfppc_7YH0y0PHaggw0cIXNSh73wVqNZmzmJx5W0_akrvy5oSz9ZB1Io2p_fTxzibefwO700bUqbElV_yuCjD7EJ_Hfqbog80y_g9TK6koX7wYwqFNQxBVavKC-HbcT7yPdvzs9hlC2MNWCT3W7gVgeYr4AFgbV9EgMcH0GtJDKYw8vkpB_vTsaSTGZAj3TNKalAwiGO50VAmF5tknF96kOrWmNL0MdkXhnm1vXgDpP68bMt4r2Qr-hNdJ4s3_nqmSDYTnZRA4qjXjrgKQfO19txt0tX7LifE1GZ1bQyS7NqHWXyMEhw6_F8pc_tS16VhvJO_FM7CX51mLLkLCGl7DsmbnEIsVUW9qlCxb6bj53UyijTYdu6uLZW9JISE2B4EevxzwDu9UGcJPHmJYi1rRQAP__jH97GiQC8FvkdAEfKqcwV9jAbBPQPG6lUkBLcoijgR3Bcwd-ta92oeZmcpoJ97PzzBbCL-NrppJ2HHQ1SMsYWoPveZTmZc66YBA0P9YfT4hZ0RQiP2gxB4snTvMFI0Ot6Q2nQ0p5DMxmWqIaCKW53rqn16AVXQeqC2TJjlbjA9sC6pr8GEGY2OQUgEmWu5GmnOSz1lNY7fNHJypChnieI_hyYiy06qouUpoHA5z_IUtfzZoMIG0yJiGUUpF9BJvYChDECCqaUM1kWnO5tKcohSKq5Hqwu_EWDRYF2tj7igSimZkS4Pts41tu8nIaVk5EkzAX9gCR2EX3Lk869mIxSyBS3MyG_NotPcbm6uXDn_YkV5Z0HkxUxYRA9hIG-UhKhK3VOaHZP8GcQN8noOMa2CnPd208X6HOzlIlxs7SRbzppUs_fHN1eROglNy-2oJWGmo-xOy0Qd44TtY0S_bYhu6iH6inrx3-yncSrWFxEiYosvYJD4ZBSyrV4d6UsfeNSHYS0ODTsdPqz4SYTeloZbIx8XWz7fxLXlNyLr3s9tp-Q25f1vTIrmQL",
                        "ulyZKDtY8Do_dXzYGyuX7BY-1dDYM4FuSiw0gFdO-eJXFHy45NUAoR0EC-oa-BYzQKUDnsmvnB6LnwTrKgLK72LjR0HtnZt33kFZNO5itZ2QP2hbioyfpga_gZzp_WhCRn80lMEvfoT34jtVS4yiW4VnbAZrLI1kNLMoPrll7_-GzZhQBAFOmkIMAjQtCSeKIKdjGhVKACAQpbWS0hUCURUhAMeJEklNIkmQyctFALZsGcQSwcKK4JSGxURuZjAwIigtILZsmamKicASwDCRILgg0IQQTABojKeOEKQQ1BGQSbdymYSCyCAq5TBs5SkIEgMIkicO4USIJkNHGMCApJAkihshAipmEAAuBRdoGKQqxRZCIKOEIhMqgbJpGCAO0JVtGgoMEhRpFASI0ENQUKYg2MkzEAKKWSAPDCZBADAw5BpyGkJQYIhDFTGCIQREyQQIBRcyohIoCSRiiAOOCTUI2ZgoGbiNCRAKmZVswgBm2KQEWaiAxCRukARkDEYAogdKAIJDGTeIIZRumBFTIZFvGMcoyZpkCjOMmgZiEgJy0hNoiAgQ1EtPIMSEnTJnAacBGiVESjKMQbNAEieIYhRglIUgCjluoAAAwCggoAVyCCCEZSkEEimNAjpm0jdkiIBkQIZPIkQiyjduEASO4REIkcAmRQSGRKQCBCFogAVIQLsgoEeNGMiA1KRozRiFJBssobCMniBtJKRilTZK0iMACcpI0iIo4QoMgipAYQKM0TiJIJJNEZhzDSaHEENsCDOAiDYrEMEGCMRQjMhM4SmESiJqkDAJJLcFEQcuWYRI2ikCicAwHCchCkZEgUhg5ARtGaQEyRNy4kAghLlPAkdLEMUwgRtoyJsogTkOCAMmybdiShdiUkMqYjInCaEyyKIQiRBFFKctGRcI4RqE4ShtCbUoQbeGSbNsgadgYLtwSEYQChMSWYdmkMWLALUOELZoSTQrBJVEgLmECQCGoSNC4AAgBKAgYESDBUdA0htNCKcEoahsAAUNGSQCYZFNAYZwyYFkwguIoIiKHMIsGSALGaQIRRkiEbAgykKDAMBBECdw0ikqmUMKSjOEikIs2KhAAKlG4DRhJKgohKYxCMKQGjphCDhrBJQHJMVMiJEAIYSPHYOEQMQLBIFw0EkI4bCQWThOUJJSSBRk0CNlCkuTARCIXRhxAjcECEVjCKaPCZVLIcql4HoeXD1OaOqFehE7e2oAeL97U_fVM2QfuMSUocWXlca1SovfqIY1cVn-HZC5kgjrFfEBw9J7VV9o10GSat_0_ZxlDRZMjZugj6hPxi0SgBTFILoBagT2g3T5oNLyDBkf2K4HfyUzuWGH9xrwU4YtT8IiVrlGsUpahlfFBm3Aykx3xXsCh9CyGqDud6JW3uDYdMHQX80OmiOcqtncQfrSWtjp4HN0TNsJH02sMX3QaIlUwkOdZfA8cFQsTctoRfjPS1yiQxnz8CIkrHHnSXLc3_Tx7WMH0t1n3XkToOfIXheEiKXNQfkSiw9YFUcyu09lNhPxIirxNYtUB3Nol_LmwsbawT1eUiWiAxjMhoa6gig87Z-DLVe_RLivg7Hw-YJswhiRSRbLMwcQf86-ntscG2lpGDP0Tzh_ME5tryC0NafdUwDwN8xBoP3uyPSru7iENwMpXeMtn6qNCLgYWYTZg6G89uU1WHJUPvMP_njwYNqVl_YYnyMxL1vqbwuUkRbXAalI9mGNDLyNpGmyOWSzftFwZEXd6Zr37z1nFQNWu9nMstx74JT5oXAIp6mcPO4t5EoI4q9Ku4kulk8WMvJo4awoq4nPmyNp3IZE6cIYw-SJDhdIoLNg1hfF2NIpIzQOuFNge3cQg3vbKlL9cV1uPOLuQZYhqLOmW4rsxh2qKdPEJvI30y2EGBiEs3-oWv_spYtckLvPk0BYVukp2w4bHZ2DC_qDA5HJkFRroHO029fZTmPmpyIbPry6fKes3MjMxEo922hXyKwooyr-C0MU837eTPx3JZuxxOu4sUO5FP5GQ42RNM43EMj_7M1nfxeZhxuWqzVnPCnngU5XVPqIEjGsG_HiTikz-KUXerygny4HlyJjazPvCiOz_OB-M7t6fYSgoo28CZ2vwelVbEcooTX1UtUW63rejU6xANv765iVgruw6TwAdOpxvDeHcOfhc31QV62X44Pl5-nAXIUczh-i-QtzOtD6o_goZ0DOWv7ovzqi9BEgMIkiTbwRMHCVpnqgqp0bpZg61ZepIzOkwaZGHbzfX1rqpZseeRDZxfheLXf0jb0Ph5vVL4f-e0mWE-0hHm7Ga3g3TUADdrVzykMeOLrceNQ-zsrVfdM4jvbbtO-3-Ip83OI7Trb9SwJyhCj1ybxchP1MNSGxRI5Mct0YDDLQNVulSF1uQyidav8nIk9NTybUwkgMo7SC0oJxiI70gRHspfWWTJAgIGof_yW37nz1HoJEtFuGbM5Q9j4lSDJe6BCCNTqBZ-ZyCigTesm6Gdhc10A9NG72WDbwY23GxxwODXZ7-yCs0fjoVB9yQHhj-9EvtHQvkXUC_eT8OkeF3Ee12_-lYJCgyhLvcrdlVf4XeV3g90ULcEcWyd4AwW3xhfw1ekdvddQ0OhRQlaOgtfIIR77-Px8kAN35fcwGaHJA30j0XfbF2QxggKBn1kZSxmPQGXwpJqDhkp3yB7g_ZaYsAzNpSAb9qUSrvLAie9fqxtZisPhRDFfIWQVSxeOUmK34XfPd0q-1SLeWbHFwP548dGtpzWZtzASLGLyGB4r27takf-44UXhnWmWFHPRToRZtMH8ldVM5OYiAmYlqufclf9Oi9kAtKziLuqkgETRRnlh4yc5OwMKAMxsqUpEeNkvI53CbmVBTm3cJHqvvksJE3MAdiXXmGKxDlG1vuMfM7LuBO2R_f-K168PnQ-DRV9Awf8DJTs7sXITxRuNQ9d6CPhXNTkWvxKJswnemHSR1Hrd6YIKDq0RQ6GKgzInRJFtMjQcRk1_uFQILjtnyd9JiAztD5JNIopTGC7t9SjnFz61oy_1E2E_5jYe6LVzJ7-ENhOiCJeWJTJTQORyi6Qf_IWcQGO03uBomHqCtdq2hcSSuSprohuzFhg3afTjR8a-7skGJ_s3zYODM4G8EkPYh4ZxieG7mvMtwBHzTUBYu_QMpqdiPmDwUh-dacRZDClO-p5IZsR9Dt-s9aMWR27A93NKtdflPUoDuODP6NyuWlqKIfvGdUwklKv9k6OK1cQn_i0CGsJj_RoCIiX-byFUMXnngy4Q07Ad2AbGq0E6TEESh6gpzVKo-ucqc-XkYSesHSbqOpOdaxdRE991tzgt05VpAtf-zRlZIKw0khtzXd3utNqiSNNPOK3nBG6-Jtqr2uWfEHof98G-dSvcyT3uPMHfAn-J-7Tu5q1D-tKRQGnpNsyzST",
                        "ffa074359f280ed66bd74b65c8f644175b2f413e24ef3b917d557b3bc261b28f203f59e5b04ab575b1172cb684f22eede72f0e9033e0b5c67d0e2506768d6ce11",
                        "unpvE-Lb-4rOZmeMMQ0pnZ4fZhUsSemAyJqVfFJcy8hg3r_-zAXy1h1iqSB5capJ9x3jZvOV5GqSYur8-7ekoJT_T5ltOc5pftD31fJl87WyqPlmRU8gPpY2ssDfM4ewMbvSNCQ_ycZta1cyO4NdMEmmI7zxSQnFfq1znO-WHNFrmvs-35Y3vgNG9OBTkFrGMhNhp0_Pxwo5PPnYT5XUJaewbePkR9q2eqD9xMfG-mdC2DDeoNSaWpP_jSluM_oI558AKLVaFdzAIjTryHwDlkKu4tn40sKvLWkEy9-oeQHBLHSQVBgJikQOdz6QyRqXIGj0cDEykvYQ3UcT21KOwLfvGHzs7bo6GGqyNhSrHNiAU5bpfPjnVsoRBlPv8ZHknwDN7ZWQIYMmJk5J0IjsvCH5G5thdxilduNkxz1mRyBCYOXG0TUT8sKhjT21m_WCB0H6EQ-eTBF-w13eKH9qezCK-CbRH926u-WncyyaurleEZ60qAo_zstf3pva8qijewH60ec0v4ShCpuv0rGsYBcQTh1_8qvZqLv2XABK-9JBiOMG4GrwEcd_wg5kU-6RCwcQSYoQwLa4qHuf2lP5T9Ta2-s1odhXfV_yjhwDKR9Kr6OAMGona7WRfvvwZfehCLyhCdHmQyoGppyvXxC0ye7IugJ0uCVH_kcbPZniMIw3xjIfp472fG3HnMj3q6f0OGGlGiO0wYPO_3KHBPR0rE9wz2DbiZ6UjyUBsGYfjx97vL67xZ_VB_AAwoP_7PCGvLAF3wUITEgm--aUve3L3ZGbySs4XiSl5jHYJZPm0MD22jW4kIFXcQAujKIA8fcDwWPMf0gkd2Z1plFokBxWk6hMOj9eG_aNUdLXTZE95ER7NfFWm0f9O6rk7DknkI_Cnk8dKWdbmNU2s0zeKOOZXO28LVlh-ln_JprH7aOiXQGV9e9fDNg73QzlGvRKFGTMcNPY4eQLe2WpIZxIXgjsur35BnWYTdLHgIFirqRrZ7za_CHyvHoMxdjxGs3tpNntMhzXJczSy4juTnOLi3_dhomYdBbVqT3yL7UtTlzymvgAjVLPDGPckISPoroSKhXMk1jI-PkC-7Q14ymN209FTsagvD3L2tfjk1t3kEghMLWyvSzqTL1yH6togtZ0LgOPheNPOst8SGZtvoTy8-nH6_MqvPy38k0nYMVdAnDR-jWYs27fS1FLsfuw6qCaPC4RrWnMbXu5P8o6_ADqGhI4MZreL4BlEWd40iK6FEQRswfN9KSEWVufo1FMWqWagJrYr3iR16fqzirKu2dssmBJG2OWTjEDzlFpQ6IysgsCycBDeomrwi_hRKi9hN3QB7HKMX1G6Mw2gvd58K0oiVIaH2WzaVE9eMn0Hy6zukL1tg7PSvD6tSPqmPR-4x6r3rkHJY3z6mg5P3xQCuNabwJd5ds2cvt8qLJrrqHPJBmnuEQEqIdI8C0SSxRgVVgv9Wqh0ZNgN40ri4odTKWARWs8MdaU-VJnXLCHtG5rZ4heeiJF1VwSrjCBvFgDV5-OIm0N8eIV8uzQj2zIhjQVcgBnVDOE2o-qKpWcCAfOxFhgcTJM2qkdAeW8yYHCatLhXdKDuIVHZXxLUTpvHOkK1xAV8Bh96LZjzWYPhHVhIOCjqOePDHDyxv8bvUmBH8b7w7HaCeAQ8mrxBzF_p8gPCSdhrwEOTNxaJQf3JaIr0nxLi3dUBT5omIkOzdMKEU57KhCzTMuBa8z-cKUN7ck4Z4icO3bL7eq8a8w2D7XabRvQPug3YjJLO2nvknQDMCtQ5kU0XBVCLQraLAsldH2RJZ3QxvYxX8JcYkQyNs2xZCO1urNsEkjJGjxqHw6IYTMzIRwjVr01mQGU2DNLhlRqYW1hEc98bSRWImLF6Z9zMldODd3mFS2EhVmeRyBX_9ZrmanqnbG3T4_t9znFVlm_rLaFNBce9D_v8ibbHIkSbAQm4fMZbuy9MpCXRIA-H_UrQjhg_XWhaaHVlHg4sLr2CX4DbUZAA7hDGMwNFiSCRqPS-NywAXQ9uvjdTO0miFQlh1ZxXgrJ-pPnwMtCSc6EPR0lvzZnuwUyvEdGXwA--jP9f23UxpSuTqt27M7zU5Yl6vcKGtDIsP3eV6usKSxaoKI2NvEysAlhlM04AH0gfk_4-K442qJZ5w9epdG3rwkLkQVw7W18iigWRXPyhZXsqsTlNZGxdlLjB_iaSHTV_4R1jsWQK2QiECGwWKJtB1QTGnEi7sYs-yFS0k5Cj3XF6wkFUtrKN6Y01i0-107yV-3a4kOCJcPe1KyNiizFaXpOwKt0FdMUKcbysUziVdtpiDYyHlDYVlWCAzslUSYhMc5IlG5_RUSubBuOcjrOOEeHrJ84Ix-g7M9Y-C4v1npcMXhiMEKykjrIVw7g5T4I1SRtKCmzMCXZuo_0qoC5X3hKuyHcK-XfPABKJbs-hO0bPYYmfq0UGGlfeu5J2D8neMGzFLproWndY_-e7PftAZ-7Q6ajvABhZEpL3GK_5iYVT5AN6JZispQTm5tGSE8dYJwZpYHsKnyLER2UV_J-3tSsQ87olyXLrsgxpESg1BYcBF1-a8h-Aur1884CqBNLG69y2dcApcpuYhl1qjLgB_MALnw5A526BFODif_x6enoO0kezaYNpUWKn0z0TcJDg8iWZ37_Ls1cu0Q9687yy2tNdfWSBrypl_xIrizpVaWMJRd6JoNa7qxAV-LrWqCuM2Rurxbrk_T6MQrJzTY29sHRt874p-rFHxJB-1Vyy7MGOgxP8XYMsZj2CJHMU-wWuEb5wJ6-2oBN69oqpCjO_QdlJEjoYUGZhKDK1_kX1IzhxOt5-v_OTwXOtYJxKkcKA6QmPqlS9_EpMvwFXLl6GBSX5ZTSGvcaF8Zq_UqtdaSMASSPRYTcWODZdbVTN71paqf3GZr0f1NxI3z4gdyIRX9g7jz1sO64odnN6aUdvv3FPx2voOmS16hxonA6f9m85ZjpAE_N9ZmZIrdr0ua-XBwTCynFuKqThq9pLL7nEsbI9VckiPx1P0Idhmvi6NBRrIDBS9WqN_L8xR8-pfZVe3jkFBRBplox_DHU8p9F0-dt5cFcakGJ1fyi8dtFGLalykkqSKtICFxwiMDM0X4SUrcPJ-QQJDg9DR1FWXHF7f4iPtc_S6gIGEyItR01uco6ws_f5ABgxND9beYiQs-b2AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA4gLjo"),
                Arguments.of(
                        "ML-DSA-44",
                        "ukCRKDtY8Do_dXzYGyuX7BY-1dDYM4FuSiw0gFdO-eJXFH0eqlt4_CP4sEISGAzNlKDzLpUJWoInRywXOpd7FCp_QAJAlL7iRo4cepKhzhlq8xt6qd5jkhYF9tNH8z3RGDl9aunNy_06fWLYNScWd5RmGg46Po8T-kIjMMkJftaqqZcGDxktpu9Et2bnaZMx4K98YyG1urUpM9lgvldgg2qv-6XCrm2uXlJ9U-HN4xtQKn4Ug-5xPwbhPGR2pbcBScTFotkhBqLc2eQLL6zPutWF83sSZbOhD_11BjMkeiLyJbMeHCIhz5GDIbPksEFIaSho3MdFo5fpQ8QZoqCit3Jn4ddfuShfIoLU1Hw5EZ0xBiqOU7e-TINd7-7HsgHLmYMGnpqljm1ot3c3cfalYsg87WQuSscO7XNH3Ewa-cgU6Bnj1SGn0plTy6Yq-GxU8XUBPwsK_IoIJbXWC0UD97c9UYpNiZi0-ECnbB-Y5_SM8auMfoIeap_buAOdXlJZmcp8xNCXI59AN9-96Sdhks5L-JmsELzyjAgjqNx8Zt3KPFc2jSwNjDVC1fEa8FdDdDT3WNkF6KTt65lb5_aIkFh20nOvT7kIJcKTgmhRGNJZgGSPYVbMypaQoaac8dtEoQjvYgnO-rM_RcsiWMHNc29br3o5wdiLXdr63MoX1lEWu_THBfeP1JuxrSbUmHOByepWbubbSM4iVQITCxBHZT0Mj2bWwIxd3nZUajzebyEnsfitV01kpzlO7bzY2uxSzyplTkRfppc_7YH0y0PHaggw0cIXNSh73wVqNZmzmJx5W0_akrvy5oSz9ZB1Io2p_fTxzibefwO700bUqbElV_yuCjD7EJ_Hfqbog80y_g9TK6koX7wYwqFNQxBVavKC-HbcT7yPdvzs9hlC2MNWCT3W7gVgeYr4AFgbV9EgMcH0GtJDKYw8vkpB_vTsaSTGZAj3TNKalAwiGO50VAmF5tknF96kOrWmNL0MdkXhnm1vXgDpP68bMt4r2Qr-hNdJ4s3_nqmSDYTnZRA4qjXjrgKQfO19txt0tX7LifE1GZ1bQyS7NqHWXyMEhw6_F8pc_tS16VhvJO_FM7CX51mLLkLCGl7DsmbnEIsVUW9qlCxb6bj53UyijTYdu6uLZW9JISE2B4EevxzwDu9UGcJPHmJYi1rRQAP__jH97GiQC8FvkdAEfKqcwV9jAbBPQPG6lUkBLcoijgR3Bcwd-ta92oeZmcpoJ97PzzBbCL-NrppJ2HHQ1SMsYWoPveZTmZc66YBA0P9YfT4hZ0RQiP2gxB4snTvMFI0Ot6Q2nQ0p5DMxmWqIaCKW53rqn16AVXQeqC2TJjlbjA9sC6pr8GEGY2OQUgEmWu5GmnOSz1lNY7fNHJypChnieI_hyYiy06qouUpoHA5z_IUtfzZoMIG0yJiGUUpF9BJvYChDECCqaUM1kWnO5tKcohSKq5Hqwu_EWDRYF2tj7igSimZkS4Pts41tu8nIaVk5EkzAX9gCR2EX3Lk869mIxSyBS3MyG_NotPcbm6uXDn_YkV5Z0HkxUxYRA9hIG-UhKhK3VOaHZP8GcQN8noOMa2CnPd208X6HOzlIlxs7SRbzppUs_fHN1eROglNy-2oJWGmo-xOy0Qd44TtY0S_bYhu6iH6inrx3-yncSrWFxEiYosvYJD4ZBSyrV4d6UsfeNSHYS0ODTsdPqz4SYTeloZbIx8XWz7fxLXlNyLr3s9tp-Q25f1vTIrmQL",
                        "ulyZKDtY8Do_dXzYGyuX7BY-1dDYM4FuSiw0gFdO-eJXFHy45NUAoR0EC-oa-BYzQKUDnsmvnB6LnwTrKgLK72LjR0HtnZt33kFZNO5itZ2QP2hbioyfpga_gZzp_WhCRn80lMEvfoT34jtVS4yiW4VnbAZrLI1kNLMoPrll7_-GzZhQBAFOmkIMAjQtCSeKIKdjGhVKACAQpbWS0hUCURUhAMeJEklNIkmQyctFALZsGcQSwcKK4JSGxURuZjAwIigtILZsmamKicASwDCRILgg0IQQTABojKeOEKQQ1BGQSbdymYSCyCAq5TBs5SkIEgMIkicO4USIJkNHGMCApJAkihshAipmEAAuBRdoGKQqxRZCIKOEIhMqgbJpGCAO0JVtGgoMEhRpFASI0ENQUKYg2MkzEAKKWSAPDCZBADAw5BpyGkJQYIhDFTGCIQREyQQIBRcyohIoCSRiiAOOCTUI2ZgoGbiNCRAKmZVswgBm2KQEWaiAxCRukARkDEYAogdKAIJDGTeIIZRumBFTIZFvGMcoyZpkCjOMmgZiEgJy0hNoiAgQ1EtPIMSEnTJnAacBGiVESjKMQbNAEieIYhRglIUgCjluoAAAwCggoAVyCCCEZSkEEimNAjpm0jdkiIBkQIZPIkQiyjduEASO4REIkcAmRQSGRKQCBCFogAVIQLsgoEeNGMiA1KRozRiFJBssobCMniBtJKRilTZK0iMACcpI0iIo4QoMgipAYQKM0TiJIJJNEZhzDSaHEENsCDOAiDYrEMEGCMRQjMhM4SmESiJqkDAJJLcFEQcuWYRI2ikCicAwHCchCkZEgUhg5ARtGaQEyRNy4kAghLlPAkdLEMUwgRtoyJsogTkOCAMmybdiShdiUkMqYjInCaEyyKIQiRBFFKctGRcI4RqE4ShtCbUoQbeGSbNsgadgYLtwSEYQChMSWYdmkMWLALUOELZoSTQrBJVEgLmECQCGoSNC4AAgBKAgYESDBUdA0htNCKcEoahsAAUNGSQCYZFNAYZwyYFkwguIoIiKHMIsGSALGaQIRRkiEbAgykKDAMBBECdw0ikqmUMKSjOEikIs2KhAAKlG4DRhJKgohKYxCMKQGjphCDhrBJQHJMVMiJEAIYSPHYOEQMQLBIFw0EkI4bCQWThOUJJSSBRk0CNlCkuTARCIXRhxAjcECEVjCKaPCZVLIcql4HoeXD1OaOqFehE7e2oAeL97U_fVM2QfuMSUocWXlca1SovfqIY1cVn-HZC5kgjrFfEBw9J7VV9o10GSat_0_ZxlDRZMjZugj6hPxi0SgBTFILoBagT2g3T5oNLyDBkf2K4HfyUzuWGH9xrwU4YtT8IiVrlGsUpahlfFBm3Aykx3xXsCh9CyGqDud6JW3uDYdMHQX80OmiOcqtncQfrSWtjp4HN0TNsJH02sMX3QaIlUwkOdZfA8cFQsTctoRfjPS1yiQxnz8CIkrHHnSXLc3_Tx7WMH0t1n3XkToOfIXheEiKXNQfkSiw9YFUcyu09lNhPxIirxNYtUB3Nol_LmwsbawT1eUiWiAxjMhoa6gig87Z-DLVe_RLivg7Hw-YJswhiRSRbLMwcQf86-ntscG2lpGDP0Tzh_ME5tryC0NafdUwDwN8xBoP3uyPSru7iENwMpXeMtn6qNCLgYWYTZg6G89uU1WHJUPvMP_njwYNqVl_YYnyMxL1vqbwuUkRbXAalI9mGNDLyNpGmyOWSzftFwZEXd6Zr37z1nFQNWu9nMstx74JT5oXAIp6mcPO4t5EoI4q9Ku4kulk8WMvJo4awoq4nPmyNp3IZE6cIYw-SJDhdIoLNg1hfF2NIpIzQOuFNge3cQg3vbKlL9cV1uPOLuQZYhqLOmW4rsxh2qKdPEJvI30y2EGBiEs3-oWv_spYtckLvPk0BYVukp2w4bHZ2DC_qDA5HJkFRroHO029fZTmPmpyIbPry6fKes3MjMxEo922hXyKwooyr-C0MU837eTPx3JZuxxOu4sUO5FP5GQ42RNM43EMj_7M1nfxeZhxuWqzVnPCnngU5XVPqIEjGsG_HiTikz-KUXerygny4HlyJjazPvCiOz_OB-M7t6fYSgoo28CZ2vwelVbEcooTX1UtUW63rejU6xANv765iVgruw6TwAdOpxvDeHcOfhc31QV62X44Pl5-nAXIUczh-i-QtzOtD6o_goZ0DOWv7ovzqi9BEgMIkiTbwRMHCVpnqgqp0bpZg61ZepIzOkwaZGHbzfX1rqpZseeRDZxfheLXf0jb0Ph5vVL4f-e0mWE-0hHm7Ga3g3TUADdrVzykMeOLrceNQ-zsrVfdM4jvbbtO-3-Ip83OI7Trb9SwJyhCj1ybxchP1MNSGxRI5Mct0YDDLQNVulSF1uQyidav8nIk9NTybUwkgMo7SC0oJxiI70gRHspfWWTJAgIGof_yW37nz1HoJEtFuGbM5Q9j4lSDJe6BCCNTqBZ-ZyCigTesm6Gdhc10A9NG72WDbwY23GxxwODXZ7-yCs0fjoVB9yQHhj-9EvtHQvkXUC_eT8OkeF3Ee12_-lYJCgyhLvcrdlVf4XeV3g90ULcEcWyd4AwW3xhfw1ekdvddQ0OhRQlaOgtfIIR77-Px8kAN35fcwGaHJA30j0XfbF2QxggKBn1kZSxmPQGXwpJqDhkp3yB7g_ZaYsAzNpSAb9qUSrvLAie9fqxtZisPhRDFfIWQVSxeOUmK34XfPd0q-1SLeWbHFwP548dGtpzWZtzASLGLyGB4r27takf-44UXhnWmWFHPRToRZtMH8ldVM5OYiAmYlqufclf9Oi9kAtKziLuqkgETRRnlh4yc5OwMKAMxsqUpEeNkvI53CbmVBTm3cJHqvvksJE3MAdiXXmGKxDlG1vuMfM7LuBO2R_f-K168PnQ-DRV9Awf8DJTs7sXITxRuNQ9d6CPhXNTkWvxKJswnemHSR1Hrd6YIKDq0RQ6GKgzInRJFtMjQcRk1_uFQILjtnyd9JiAztD5JNIopTGC7t9SjnFz61oy_1E2E_5jYe6LVzJ7-ENhOiCJeWJTJTQORyi6Qf_IWcQGO03uBomHqCtdq2hcSSuSprohuzFhg3afTjR8a-7skGJ_s3zYODM4G8EkPYh4ZxieG7mvMtwBHzTUBYu_QMpqdiPmDwUh-dacRZDClO-p5IZsR9Dt-s9aMWR27A93NKtdflPUoDuODP6NyuWlqKIfvGdUwklKv9k6OK1cQn_i0CGsJj_RoCIiX-byFUMXnngy4Q07Ad2AbGq0E6TEESh6gpzVKo-ucqc-XkYSesHSbqOpOdaxdRE991tzgt05VpAtf-zRlZIKw0khtzXd3utNqiSNNPOK3nBG6-Jtqr2uWfEHof98G-dSvcyT3uPMHfAn-J-7Tu5q1D-tKRQGnpNsyzST",
                        "f1f49de8352bfcdef9457b14be9f4375c7288fb914cf1c974eab20f3d145b011a6ca388adaff807c71d063f666548493ba60c8c0fa109b3dd1e2564d61abe09cc",
                        "uTSucVLvXmOpmjGGNB-B9rM-u4HzBxN8ZIuZbpTHrjOTNBnahoE4PSdkeD-IzLLXykJn0aYq_APExy-Ka0BcJNMvKgkdjbbP33WmUwkzljno3szRUDrN9KX2DMH7j0iOBakU4ByjD-hTSO1iR6rlxsZPHJM1H-WLMhzVSggBILAuglItzstl663Gz5bFjEfbKAgfe50L4v4PjLFSDbJYcg65GtCKRXISkWrnJRuToWwvTVdcnIBOQwPBFKsvApPJMKrUTkIuZf4-V1uJ81zzct4o20O-DqLQ5bHfOR2n5Y4DSy6e5zg0-S3ADKtMtuPaQ8cAPUTEKRXGRQnSndnrtgMh2dimvpSaaw0TDy7zY6vrDxJa1tkrS0ulKf3Xz8xsNrNIkx4SaKYWPTjhRvdKqjdrpbGRt3mRSFFc0VE8vK44F_EVFIhwouL-4Rm4mXU2QkiO0YkwuAJM-QdWUACqzJ7TSf2QrrU8zAwOLbrGS5uZ1qLGD1PcgWfg2d0zTAYmcWP4LP63fTnFxwr-L0N_3MLFXixHNEp5osMlo2lhl5noDCmQpqCgluxkd5gXs1NSpOBbWVQyYWcj0WMBtMam8AeqXpA39L7oqvYxqbEpiwvKrmHsXIEZrnsHKCk2P0yc10AFCCtsIapvTHwIAjbDhX11HFU5cci4X5vCdG2BUzRsgmGeiYiUClCHmqsBW4z2GA9r0d9jtHZ03nMie_qS95XPsXuAFqypsP1HOfcIUAHHS9Wn4XGFz3hXoqMsmoUGRg9vEpC2j_nkcYZQphYLs54veWq5BBzoMqPuvYhhRdawdCnn-LTf7AxQgVGoRTpTy4IkXxr_pC1LUJZJkdKeG-2TuQzyHSkbMPu3YbWsGy2KxdGeFN2yUI8TTQ-MFHl-_jDCRBrAYyCVOgML4NAtbGqvs7h2tGZYI-m9MfjG0vjp7CUyIPD8BV-Yhku_bHd0hcrseKtYYyUjxISf2wveo4dfQ2AnCVdDAbBmznjPIDlkqx0316sRc-vXJGRQmfXOW1dNk-7WNBrJVQbnT9m6cf0UEl3mEgagk1_lLOxTgjzZRpWcOB827VB3hPi7RdI6U6knXuOflHPt9BZN7i6OAl76k69uMFH2KNH3Abm0GDhOv_nu1lEaOH8aXdOqL3U8Yo0cp5roOoTw5fJP2gxwI3DY0TOWeNOCfLXmnodgoGaKG2Vns4_-gN_Mg7g0ZinguwJMwKACx07H__ffh8jYQdc87EjCNyH4m8hJICvcC81J7CKb89YTZm7IM0D1_qTR5t-DkuU4ypxNuFOCxWpN9y2QiLAAobDdc1Y_S3nXFFkLmsn7hUNhcgXxPC3jLifiM0IV7DAqmQpk2ZGE59l0VTKb3F2Ualj9JcqKtLg_b2KqprUol9WtjFlkbxqJPYyCKnSEitzDnDsfxTRFEIViTx5-1SFb0NjPE_hv5MewCkizNpfo0b-m-FxvyWJnDYt4Igv8JtgF0K_xMRC9Tf3NaQgFHb1OgkBz04C3wsxoPLqTgMWoxcZ2-2x7TRRvX2Nh1Ye-ZrpmF5hVeRMK0ECj_t5HLPaq2md18rqnhwsZv84-V0eReDyXVIhkE2eAKedCM9t13UjTfF1qFoUQ3D8xQ6MfR7zFwf6X78Tb0EFs0cBQ1TatzWysbE2b_0k-YbT78G4Ko8FlmTljBN1b0StKxzOE1Kp1h4nDBY9jZYYPNnVrtAGn2AKN3HWr0bhhF8fW_G4SA_MGu2r6LnobB3MLCSj1lbVZSk5YtCtMnkldAsDagoI8lRBlKW1R7PFvXatSHcwbe352nVuvZYxs9QJVSylf2QS1xeUQUMS4AHd4h8Y9HqmnGPr5JX65uch8sr1bWcpGiNlwnMlFy89pnEZU2v7IiC_foLi8JbxMud1k2XRDwThhepEf5bxqlBcgjF8vAbGEKAJg5Oahl0GCBffuHhuUXmuF6XwOxLovlJUM8oFCTayQL42NGz_Z2cuFltmiy-cbI8NmEpvlOPnGZBj00ZIrp9tAgwepYvlt5ticFn9ufU-f8xZpBpZb-rf5sVTNqYmoOYvhKVhE5cCPpCbzZ5GvW5ukB8yLLJC5sc6df9oSoujovfA_VAXqsvmBuKU4cHcNTNEqzsGEg_l0ln5FIHKH3CRUTDemKN2vbtmTz1snn4VfdBFAlhJhBItpBmd3HibH-1q2WD313j-cE5nW_QgQeDn_JFJ7zwlORQVRUkeB942HJjkHWXCJMXz9LKxdncKalaVNm-yVjDkQdgA1tKl9bc_QnAL7HWhtHFc_XhzJvQxqLJ0aLUkkrnphrqscG6D_Kdh7aTTkDjDSA-dmgRQBh51YVBnfnp5V28AwmGXXglBAWCWChGmAtac6xeLbxW143426J4HMAUIpLgNhjetQoQqKzVTIpzcyo-GK8L3C0calt57orTswSRqDjxg_6zAQ6RPNoThToRqb2QgHr-gOop6NEJkEy0K8GwPg3nYAFgVVjcCyDtMEbIrHXJ9WKg3oTd14eC7GNZgQ75aP3HpUAqT5gqWdxer35Ohs3n1FylwreS1kOZ5Z4OVW5PVJkNHhKPfaNq4mQT5vBAWlWphOotPHwTbN7oMiOGYu-AMTnsLPCn0A3VEXx16EROpu0zlVisEZo1sya8nQaJYiI_i3MEWqvV2ypvcMDYt_ArFjxMU3tjV5tNcJgIoE5E5UCGyo2HQMvN03T0GdHZr7txswg9HpRJqntiJzm0iAr9BhRPfErg4HLQyc92gOH6UdczP4hvbweP3mcW67yUT3lH31vznZ5lIJ0pth3H_7khwUff5daIROar2usWxoMWTItY5HC7v5HBjnfqj4EoVi1A4Uw7RvHhaCMkfDrqqntNM0TDDSfDP1uCB1RwZtcuWpNfLWYyP1B9XqwKg3EworHhIq1vI74gZROvebyYqx8UCFeiLubTfXHJrC2evT99ha6jf7vg8zKgew6Cj3Jz_RSRE5rF5uQQno6PsevbKKtZsQmn0PQphfNzWqacMpred2yrmetGOEG_JvYxx5Scmu8w8Yg-3V7yhMeDsOh0DZ8sjTw5d-w3zKUNeU2IZzz5wvaJ7-8RRxyJqxFsUFaBv7WxUZ0bmWg4pRXdBUKNW53mEUGDigpRF6Fla-6wc4BCDhwfJWdpaixu8Lf4fkRKCs0Y4-lrsTH1-zwNkKBudbd6v4AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAwbKDA"),
                Arguments.of(
                        "ML-DSA-44",
                        "ukCRKDtY8Do_dXzYGyuX7BY-1dDYM4FuSiw0gFdO-eJXFH0eqlt4_CP4sEISGAzNlKDzLpUJWoInRywXOpd7FCp_QAJAlL7iRo4cepKhzhlq8xt6qd5jkhYF9tNH8z3RGDl9aunNy_06fWLYNScWd5RmGg46Po8T-kIjMMkJftaqqZcGDxktpu9Et2bnaZMx4K98YyG1urUpM9lgvldgg2qv-6XCrm2uXlJ9U-HN4xtQKn4Ug-5xPwbhPGR2pbcBScTFotkhBqLc2eQLL6zPutWF83sSZbOhD_11BjMkeiLyJbMeHCIhz5GDIbPksEFIaSho3MdFo5fpQ8QZoqCit3Jn4ddfuShfIoLU1Hw5EZ0xBiqOU7e-TINd7-7HsgHLmYMGnpqljm1ot3c3cfalYsg87WQuSscO7XNH3Ewa-cgU6Bnj1SGn0plTy6Yq-GxU8XUBPwsK_IoIJbXWC0UD97c9UYpNiZi0-ECnbB-Y5_SM8auMfoIeap_buAOdXlJZmcp8xNCXI59AN9-96Sdhks5L-JmsELzyjAgjqNx8Zt3KPFc2jSwNjDVC1fEa8FdDdDT3WNkF6KTt65lb5_aIkFh20nOvT7kIJcKTgmhRGNJZgGSPYVbMypaQoaac8dtEoQjvYgnO-rM_RcsiWMHNc29br3o5wdiLXdr63MoX1lEWu_THBfeP1JuxrSbUmHOByepWbubbSM4iVQITCxBHZT0Mj2bWwIxd3nZUajzebyEnsfitV01kpzlO7bzY2uxSzyplTkRfppc_7YH0y0PHaggw0cIXNSh73wVqNZmzmJx5W0_akrvy5oSz9ZB1Io2p_fTxzibefwO700bUqbElV_yuCjD7EJ_Hfqbog80y_g9TK6koX7wYwqFNQxBVavKC-HbcT7yPdvzs9hlC2MNWCT3W7gVgeYr4AFgbV9EgMcH0GtJDKYw8vkpB_vTsaSTGZAj3TNKalAwiGO50VAmF5tknF96kOrWmNL0MdkXhnm1vXgDpP68bMt4r2Qr-hNdJ4s3_nqmSDYTnZRA4qjXjrgKQfO19txt0tX7LifE1GZ1bQyS7NqHWXyMEhw6_F8pc_tS16VhvJO_FM7CX51mLLkLCGl7DsmbnEIsVUW9qlCxb6bj53UyijTYdu6uLZW9JISE2B4EevxzwDu9UGcJPHmJYi1rRQAP__jH97GiQC8FvkdAEfKqcwV9jAbBPQPG6lUkBLcoijgR3Bcwd-ta92oeZmcpoJ97PzzBbCL-NrppJ2HHQ1SMsYWoPveZTmZc66YBA0P9YfT4hZ0RQiP2gxB4snTvMFI0Ot6Q2nQ0p5DMxmWqIaCKW53rqn16AVXQeqC2TJjlbjA9sC6pr8GEGY2OQUgEmWu5GmnOSz1lNY7fNHJypChnieI_hyYiy06qouUpoHA5z_IUtfzZoMIG0yJiGUUpF9BJvYChDECCqaUM1kWnO5tKcohSKq5Hqwu_EWDRYF2tj7igSimZkS4Pts41tu8nIaVk5EkzAX9gCR2EX3Lk869mIxSyBS3MyG_NotPcbm6uXDn_YkV5Z0HkxUxYRA9hIG-UhKhK3VOaHZP8GcQN8noOMa2CnPd208X6HOzlIlxs7SRbzppUs_fHN1eROglNy-2oJWGmo-xOy0Qd44TtY0S_bYhu6iH6inrx3-yncSrWFxEiYosvYJD4ZBSyrV4d6UsfeNSHYS0ODTsdPqz4SYTeloZbIx8XWz7fxLXlNyLr3s9tp-Q25f1vTIrmQL",
                        "ulyZKDtY8Do_dXzYGyuX7BY-1dDYM4FuSiw0gFdO-eJXFHy45NUAoR0EC-oa-BYzQKUDnsmvnB6LnwTrKgLK72LjR0HtnZt33kFZNO5itZ2QP2hbioyfpga_gZzp_WhCRn80lMEvfoT34jtVS4yiW4VnbAZrLI1kNLMoPrll7_-GzZhQBAFOmkIMAjQtCSeKIKdjGhVKACAQpbWS0hUCURUhAMeJEklNIkmQyctFALZsGcQSwcKK4JSGxURuZjAwIigtILZsmamKicASwDCRILgg0IQQTABojKeOEKQQ1BGQSbdymYSCyCAq5TBs5SkIEgMIkicO4USIJkNHGMCApJAkihshAipmEAAuBRdoGKQqxRZCIKOEIhMqgbJpGCAO0JVtGgoMEhRpFASI0ENQUKYg2MkzEAKKWSAPDCZBADAw5BpyGkJQYIhDFTGCIQREyQQIBRcyohIoCSRiiAOOCTUI2ZgoGbiNCRAKmZVswgBm2KQEWaiAxCRukARkDEYAogdKAIJDGTeIIZRumBFTIZFvGMcoyZpkCjOMmgZiEgJy0hNoiAgQ1EtPIMSEnTJnAacBGiVESjKMQbNAEieIYhRglIUgCjluoAAAwCggoAVyCCCEZSkEEimNAjpm0jdkiIBkQIZPIkQiyjduEASO4REIkcAmRQSGRKQCBCFogAVIQLsgoEeNGMiA1KRozRiFJBssobCMniBtJKRilTZK0iMACcpI0iIo4QoMgipAYQKM0TiJIJJNEZhzDSaHEENsCDOAiDYrEMEGCMRQjMhM4SmESiJqkDAJJLcFEQcuWYRI2ikCicAwHCchCkZEgUhg5ARtGaQEyRNy4kAghLlPAkdLEMUwgRtoyJsogTkOCAMmybdiShdiUkMqYjInCaEyyKIQiRBFFKctGRcI4RqE4ShtCbUoQbeGSbNsgadgYLtwSEYQChMSWYdmkMWLALUOELZoSTQrBJVEgLmECQCGoSNC4AAgBKAgYESDBUdA0htNCKcEoahsAAUNGSQCYZFNAYZwyYFkwguIoIiKHMIsGSALGaQIRRkiEbAgykKDAMBBECdw0ikqmUMKSjOEikIs2KhAAKlG4DRhJKgohKYxCMKQGjphCDhrBJQHJMVMiJEAIYSPHYOEQMQLBIFw0EkI4bCQWThOUJJSSBRk0CNlCkuTARCIXRhxAjcECEVjCKaPCZVLIcql4HoeXD1OaOqFehE7e2oAeL97U_fVM2QfuMSUocWXlca1SovfqIY1cVn-HZC5kgjrFfEBw9J7VV9o10GSat_0_ZxlDRZMjZugj6hPxi0SgBTFILoBagT2g3T5oNLyDBkf2K4HfyUzuWGH9xrwU4YtT8IiVrlGsUpahlfFBm3Aykx3xXsCh9CyGqDud6JW3uDYdMHQX80OmiOcqtncQfrSWtjp4HN0TNsJH02sMX3QaIlUwkOdZfA8cFQsTctoRfjPS1yiQxnz8CIkrHHnSXLc3_Tx7WMH0t1n3XkToOfIXheEiKXNQfkSiw9YFUcyu09lNhPxIirxNYtUB3Nol_LmwsbawT1eUiWiAxjMhoa6gig87Z-DLVe_RLivg7Hw-YJswhiRSRbLMwcQf86-ntscG2lpGDP0Tzh_ME5tryC0NafdUwDwN8xBoP3uyPSru7iENwMpXeMtn6qNCLgYWYTZg6G89uU1WHJUPvMP_njwYNqVl_YYnyMxL1vqbwuUkRbXAalI9mGNDLyNpGmyOWSzftFwZEXd6Zr37z1nFQNWu9nMstx74JT5oXAIp6mcPO4t5EoI4q9Ku4kulk8WMvJo4awoq4nPmyNp3IZE6cIYw-SJDhdIoLNg1hfF2NIpIzQOuFNge3cQg3vbKlL9cV1uPOLuQZYhqLOmW4rsxh2qKdPEJvI30y2EGBiEs3-oWv_spYtckLvPk0BYVukp2w4bHZ2DC_qDA5HJkFRroHO029fZTmPmpyIbPry6fKes3MjMxEo922hXyKwooyr-C0MU837eTPx3JZuxxOu4sUO5FP5GQ42RNM43EMj_7M1nfxeZhxuWqzVnPCnngU5XVPqIEjGsG_HiTikz-KUXerygny4HlyJjazPvCiOz_OB-M7t6fYSgoo28CZ2vwelVbEcooTX1UtUW63rejU6xANv765iVgruw6TwAdOpxvDeHcOfhc31QV62X44Pl5-nAXIUczh-i-QtzOtD6o_goZ0DOWv7ovzqi9BEgMIkiTbwRMHCVpnqgqp0bpZg61ZepIzOkwaZGHbzfX1rqpZseeRDZxfheLXf0jb0Ph5vVL4f-e0mWE-0hHm7Ga3g3TUADdrVzykMeOLrceNQ-zsrVfdM4jvbbtO-3-Ip83OI7Trb9SwJyhCj1ybxchP1MNSGxRI5Mct0YDDLQNVulSF1uQyidav8nIk9NTybUwkgMo7SC0oJxiI70gRHspfWWTJAgIGof_yW37nz1HoJEtFuGbM5Q9j4lSDJe6BCCNTqBZ-ZyCigTesm6Gdhc10A9NG72WDbwY23GxxwODXZ7-yCs0fjoVB9yQHhj-9EvtHQvkXUC_eT8OkeF3Ee12_-lYJCgyhLvcrdlVf4XeV3g90ULcEcWyd4AwW3xhfw1ekdvddQ0OhRQlaOgtfIIR77-Px8kAN35fcwGaHJA30j0XfbF2QxggKBn1kZSxmPQGXwpJqDhkp3yB7g_ZaYsAzNpSAb9qUSrvLAie9fqxtZisPhRDFfIWQVSxeOUmK34XfPd0q-1SLeWbHFwP548dGtpzWZtzASLGLyGB4r27takf-44UXhnWmWFHPRToRZtMH8ldVM5OYiAmYlqufclf9Oi9kAtKziLuqkgETRRnlh4yc5OwMKAMxsqUpEeNkvI53CbmVBTm3cJHqvvksJE3MAdiXXmGKxDlG1vuMfM7LuBO2R_f-K168PnQ-DRV9Awf8DJTs7sXITxRuNQ9d6CPhXNTkWvxKJswnemHSR1Hrd6YIKDq0RQ6GKgzInRJFtMjQcRk1_uFQILjtnyd9JiAztD5JNIopTGC7t9SjnFz61oy_1E2E_5jYe6LVzJ7-ENhOiCJeWJTJTQORyi6Qf_IWcQGO03uBomHqCtdq2hcSSuSprohuzFhg3afTjR8a-7skGJ_s3zYODM4G8EkPYh4ZxieG7mvMtwBHzTUBYu_QMpqdiPmDwUh-dacRZDClO-p5IZsR9Dt-s9aMWR27A93NKtdflPUoDuODP6NyuWlqKIfvGdUwklKv9k6OK1cQn_i0CGsJj_RoCIiX-byFUMXnngy4Q07Ad2AbGq0E6TEESh6gpzVKo-ucqc-XkYSesHSbqOpOdaxdRE991tzgt05VpAtf-zRlZIKw0khtzXd3utNqiSNNPOK3nBG6-Jtqr2uWfEHof98G-dSvcyT3uPMHfAn-J-7Tu5q1D-tKRQGnpNsyzST",
                        "f1f49de8352bfcdef9457b14be9f4375c7288fb914cf1c974eab20f3d145b011a6ca388adaff807c71d063f666548493ba60c8c0fa109b3dd1e2564d61abe09cc",
                        "ucRShLOcer438TkaQq3gigq3KqPw6lqeBdIRSyJVtb85pTwspaNlto8mtRQRcCxfnw8VHepvExIFcscOpzkHiqfSihw1rt0q-zeAiA5iM4DmFAailrcKXXD8aV-8PvyGbNXFJTFP0D4Fgk_Adef_yIrTrZKCil58cZx0f2QQxeQmiOZC80U0JLUtxBL-pxqcD0srCzPfTTO-irdEUHC4VgqSXgB4Q6_Pj2Dn-AjJl14zJSW-M7IceyQwrzGqHwNRBThG7x8GH8vckXqcD5DMVnNXgRn-PVRbhpn87TZWzzlsXcPqnIPF4WQ9JnaY4IkRSkQ-SFA9FDqBEXmoHiQepJBckgsS_Hr_0lezA3S5Ozx8tieOWlcjd_-yk0dOXcW4AJEtYfSq7UHGqx5mNGDWhMVaBS4Y5D_Jm8F7v996gbI4UfIXmAAOcHwq_2t_jKK6D5HRmeXYVT2DkQSB4yftFVAnl7NjnxKqzGwWAnVsN00818ib_jaKjEST5sGsH9wMKAfUiqhqkbS2F8O0208LNb_UIrHl11I1wrb9Ntx2_5LkildH4VJprcQaUWUCRU5aLi7gkVacs6YfcPmC4AIHR7AMoR6aH_k2Y3oPdWAvG0j48JabKvTAqXihIPexic3PqeviAUY79e2L7fMNnfsS0D4KnQagSFpTigFsf9jExMlcNZ-PMIerfHbLLyv-n1mwt406oSzdQDgik8rsZ3ELMHL2iY3D0vIFGYKnwwxDT05ljNYTdKzNz4ppNeSyf2DPOf3bvQi0NmRfMNm4pxX4DOYLFOclmAy2m_YbIQLu4DcmkIzYHqPp9DjS0nHqkFH4uvMByDykmAClZSNxah9AoUc_AhxlNj5Z_qx1pBaWuauaghUKcAy3gdA3F3F9VlDBF0QaaXc5Ko9yBgktsPOxzAXPM6Ye8r1rufSTjmHyGzyXxLEIDPXi4fTbL1DZ_nUtQa5bJdKwgWCpxMKMPbridIIbC38gFJ9B7ApqghWSkEifq0NKxzXHY0K3o6GQaRJMEw-mzwE8RSRFZS-_ThmwAWLb2UYHsd43XSPsKdWEkAeP6nNi7TXuztt56BkKiUv-FhXS95rmZZPPo5DITn9riJ9oycwewhCW9D9Gdv930lvjzIxdLjRhGADUf9F-fbz-ktffFjZkA3GqWpb446SqHGCk0TBLttslfQLh7JXuskXS2k9QCBPZ9MTnv_GPg_E-EueLJHX-E8F56N6K6nhPiB1AJkZFuOI2RzlrhsA7b7QBs8M8OfmyDraHCp_gjnF9cdpQLv2Bhs8NJLW1SvYvKrig9LOk5uIxjsy8jnMHRKFy313Z2rlv76uyB_7uHNFEHkRXXRSlewKjq39_yeJhVKwXIL4RbQBvDjYRSDL4Q83R1Bke5ROkB5p4w7OhBGOZGiQ_B2ee1YVEgJ-G-3IoesKpKBCHYtn6NO6IEcXWcJxlem8XprHETq1hxL8PpB1hzUCIwG3zOPQZkQOY-zpy6lpu2E6QAb1LIRK7BfFq9jGtVsb0sM1zP6j0rKvVLx4Xeq1EW7uxsORk_NXk4hF_4vlkDrl24t_XKZ9XOJVhMenSVOiywU1HJPw7GrUes1cm4Lm5vVXbUmZPVKGED_M-VwkvtX396mFBTD_VzrT1H_SRMmA8Alie9Iz38zkb3C8TjXG5V-ejpDeU517UY_41VyT4LHyINwSzAPMmQvBEhffsbLLN_t0UUjmFcH1VhK_VmtdNpFv_74TniC0gXptinhrC19WRgNk7eUR2Q4R-FQM-m6xFwqrp38RHoWT7fz7Z8-NCZmpdxtNvSqZ1NQfLu6RBbcEMJJYwtPtDcNp-sk13PAe_UA8JauYuM9kXtGYu2eUrETq3UU_CC6h4iZiEdSWPqs3gd9h8uYlJPRHycHQUsVdtwKAozhcCAiHpEm07F5WLDJfGOv2aNRyfetzWeKaPyv0sZmA1oWE0hozK_xv1n_zUmOk6UMiYgehgy9k_-zhqtYNSEgUE0hBDHvkNhT1N5GbZnnxTN08FTiHEI31knfwdlNG5XtTEzG3taf6LcnU5MORrqmYOGxQHSDBSclK0ET1K4z0XAiaTsdoJMUAHbqSxVuSKpz9faA6qwLDh0gMN1xaGWW07K6YtrU3tTrdohKjsmELg1VD83Z-rrg7PuM79ZAaEIt5wgQ50HqJeiXjZrCi36UMJswtr6K1Y1osyBPugs-p6aQOits6jKdiI1881nkggPWRlR05LzfZ-RneiYqSp9-k3p4RqBsOZbiBybCvPUho5B_-SUbCiy9g4yNLvmXL0FJzqsNCsc45VrpP2dZ-HsUG2xqOjmS9RF9SjfCaDFCtlfuhwjD2acsTHnQG0GvJw5s87CzM8eE4VvfcAYogjm8gPeqpQ_OWkaqrbhHDBpHsZwzRgVD_lwiFuamZEs2VKRA7n1kVsV57COgcouIKv0_8CYUDU05TO4s9aPvKU13TngqVxsb7EGLz0CAPbx1XNdoEJVx2TSlJY0qYyFiSS5sd9i6SqJ23DV1hxDZrkchJWSPYaj_TKhQ3Rw0X3wsz4GTQuGukNkf2CeyaUv2lMtF9BEnnXpoDd1Ae65rvQrcgAb8-Uaz7urEhqz7YyuVS9cXMNxi-VxZIHmpHFnL5fUxkkNV_a8Mfb06FGsCjLZhcZ80KLn7aMGvJr2UDS5qOD1lpKreitIoulC2hs7w4JpDLtP3cPkP9P5YwdNemd5bH8-ac17mzLWIv6VrTDuC3VGtn3qCzPWeLnmgR5JIcd_AAB4zTDAewszxgpx_H8CkuvmZZvDoKgTciqxfwC_ZkVc8ZGQf3ztyZy95ls_FBQ5I3goTjZWpZsGDe5CEiAreh7G-FpioiVT8FX3t5gqS7t8whPYa8NXoujPsTWLmhCsjRCnnU6gQAstcdNHsANCBmCmHZA_iBtZprAU6YvjRw0irpNWabWxN5rNm-1OTHgItVOcaBWsM1Tdpjnz_LPZ0M6iYPJ9kYOXthwjy_F85CRCNEGFyQuPFNsp3Sia_MOj_eFGnsrEcsJ7cOBzvENWMKT8MJf52BykuSPL0XD0wFbyL_Ysx89up8fnpKMTwJLTBnhdOyWT_UeIXMg0Wh8VKlHZBLv7aXgAGL8MFRYaMURFZXaSvdTwBRETFCE5PVxje5OgpcPN2QcsRE9ca3qNm6u8wtreDicwM0xyeoGMkJGU0-D9AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA0dKzo"));
    }

}
