
| Aspect | SIC (Signed Individual Claims) | SHoC (Signed Hash of Claims) | Path-Flattened SMT (Signed Merkle Tree) |
| --- | --- | --- | --- |
| Issuer Signing Operations | $N_{\text{claims}}$ signature operations | 1 signature operation | 1 signature operation |
| Base Proof Contents | List of signatures (one for each claim) | List of salts, list of salted hashes, signature over both lists | Merkle tree claim hashes and signature over the Merkle root |
| Base Proof Size | $N_{\text{claims}} \times S_{\text{sig}}$ | $N_{\text{claims}} \times (S_{\text{salt}} + S_{\text{hash}}) + S_{\text{sig}}$ | $N_{\text{claims}} \times S_{\text{hash}} + S_{\text{sig}}$ |
| Derived Proof Contents | Sub-list of signatures for revealed claims | All salts, all salted hashes, and 1 signature | Revealed claims, Merkle authentication paths (sibling hashes), 1 root signature |
| Derived Proof Size | $R \times S_{\text{sig}}$ | $N_{\text{claims}} \times (S_{\text{salt}} + S_{\text{hash}}) + S_{\text{sig}}$ | $R \times S_{\text{claim}} + N_{\text{auth}} \times S_{\text{hash}} + S_{\text{sig}}$ |
| Scaling Complexity | $O(R)$ linear with revealed claims | $O(N_{\text{claims}})$ fixed regardless of disclosure | $O(R \log_2 N_{\text{claims}})$ logarithmic |
| Single Claim Disclosure ($R = 1$) | Most efficient: requires only 1 signature ($S_{\text{sig}}$) | Highest overhead: requires all $N_{\text{claims}}$ salts and hashes | Requires $S_{\text{sig}} + \lceil \log_2 N_{\text{claims}} \rceil \times S_{\text{hash}}$ |
| Multi-Claim Disclosure ($1 < R < N$) | Scales with $R \times S_{\text{sig}}$; inefficient as $R$ grows | Fixed overhead; inefficient for sparse disclosures | Outperforms both as $R$ grows due to overlapping authentication paths |
| Full Disclosure ($R = N$) | Highest overhead: requires $N_{\text{claims}} \times S_{\text{sig}}$ | Requires $N_{\text{claims}} \times (S_{\text{salt}} + S_{\text{hash}}) + S_{\text{sig}}$ | Lowest overhead: intermediate hashes vanish, leaving only $S_{\text{sig}}$ |
| Structural Privacy | Hides total claim count ($N_{\text{claims}}$) and unrevealed values | Leaks total claim count ($N_{\text{claims}}$); hides unrevealed values | Hides unrevealed values; path-flattening hides tree depth metadata |
