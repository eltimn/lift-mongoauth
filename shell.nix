with import <nixpkgs> {};
stdenv.mkDerivation rec {
  name = "env";
  env = buildEnv { name = name; paths = buildInputs; };
  buildInputs = [
    openjdk
    sbt
    nodejs-8_x
    yarn
    docker_compose
  ];
}
