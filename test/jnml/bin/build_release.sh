#!/bin/sh
#
# Run this from jnml top level directory (e.g. trunk/jnml).

set -e

error()
{
    if [ ! -z "$1" ]; then
	echo >&2 $1
    else
        echo >&2 "Usage: build_release.sh [--lib] [--examples] [--doc] confm | inm"
    fi
    exit 1
}

lib=no
examples=no
doc=no

while [ $# -gt 0 ]
do
    case "$1" in
	--lib)
	    lib=yes
	    shift;;
	--examples)
	    examples=yes
	    shift;;
	--doc)
	    doc=yes
	    shift;;
	--)
	    shift;
	    break;;
	-*)
	    error "$1: Unknown option";;
	*)
	    break;;
    esac
done

product=$1

if [ "${product}" != inm -a "${product}" != confm ]; then
   error
fi

if [ "${lib}" = no -a "${examples}" = no -a "${doc}" = no ]; then
    lib=yes
    examples=yes
    doc=yes
fi

if [ "${product}" = inm ]; then
    vsn="`cat vsn.mk | grep INM | sed s/INMVSN=//`"
else
    vsn="`cat vsn.mk | grep CONFM | sed s/CONFMVSN=//`"
fi

pkg_top="${product}-${vsn}"
target="target/${pkg_top}"
tar_opts="--owner=0 --group=0"
native_system=`uname -s | tr '[A-Z]' '[a-z]'`
case $native_system in
    darwin)
	tar_opts=;;
esac

# Build
if [ "${lib}" = yes ]; then
    if [ "${product}" = inm ]; then
        echo "Building INM..."
        ant inm
    else
        echo "Building ConfM..."
        ant inm confm
    fi
fi

if [ "${doc}" = yes ]; then
    echo "Building doc..."
    ant javadoc user_guides
fi

# Prepare release package builds
rm -fr target
mkdir -p ${target}

if [ "${lib}" = yes ]; then
    echo "Preparing lib package..."
    cp system/README.${product}.target ${target}/README
    mkdir ${target}/ganymed
    cp ganymed/*.txt ${target}/ganymed/
    cp ganymed/ganymed-ssh2-build251beta1.jar ${target}/ganymed/
    cp build/jar/INM.jar ${target}/
    if [ "${product}" = confm ]; then
        cp build/jar/ConfM.jar ${target}/
    fi
fi

if [ "${examples}" = yes ]; then
    echo "Preparing example package..."
    mkdir ${target}/examples/
    if [ "${product}" = inm ]; then
        cp -r examples/inm_intro/ ${target}/examples/
        find ${target}/examples/ -name build.xml \
            -exec sed -i "" -e 's#\.\./\.\./build/jar#..#g' -e 's#\.\./\.\./ganymed#../ganymed#g' {} \;
    else
        cp -r examples/inm_intro/ ${target}/examples/inm/
        cp -fr examples/dhcp/ ${target}/examples/dhcp/
        find ${target}/examples/ -name build.xml \
            -exec sed -i "" -e 's#\.\./\.\./build/jar#../..#g' {} \;
    fi
    find ${target}/examples/ -name '*.java' -exec sed -i "" 's/M3_.*//g' {} \;
fi

if [ "${doc}" = yes ]; then
    echo "Preparing doc package..."
    mkdir -p ${target}/doc/javadoc
    if [ "${product}" = inm ]; then
        cp -r build/javadoc/inm/* ${target}/doc/javadoc/
    else
        cp -r build/javadoc/confm/* ${target}/doc/javadoc/
    fi
    cp doc/user_guides/output/pdf/${product}_user_guide-${vsn}.pdf \
        ${target}/doc/user_guide-${vsn}.pdf
    (cd ${target}/doc && ln -s user_guide-${vsn}.pdf user_guide.pdf) || exit 1
fi

# Build release packages
find ${target} -depth -name .svn -exec rm -rf {} \;

if [ "${product}" = inm ]; then
    jar_files="${pkg_top}/INM.jar"
else
    jar_files="${pkg_top}/INM.jar ${pkg_top}/ConfM.jar"
fi

if [ "${lib}" = yes ]; then
    (
        cd target
        lib_pkg="${pkg_top}.lib.tar"
        content="${jar_files} ${pkg_top}/ganymed/"
        #content="${jar_files} ${pkg_top}/ganymed/ ${pkg_top}/README"
        echo "Creating lib package: `pwd`/${lib_pkg}.gz"
        (tar -cf ${lib_pkg} ${tar_opts} ${content} && gzip -9 ${lib_pkg})
    ) || exit 1
fi

if [ "${examples}" = yes ]; then
    (
        cd target
        ex_pkg="${pkg_top}.examples.tar"
        content="${pkg_top}/examples/"
        echo "Creating example package: `pwd`/${ex_pkg}.gz"
        (tar -cf ${ex_pkg} ${tar_opts} ${content} && gzip -9 ${ex_pkg})
    ) || exit 1
fi

if [ "${doc}" = yes ]; then
    (
        cd target
        doc_pkg="${pkg_top}.doc.tar"
        content="${pkg_top}/doc/"
        echo "Creating doc package: `pwd`/${doc_pkg}.gz"
        (tar -cf ${doc_pkg} ${tar_opts} ${content} && gzip -9 ${doc_pkg})
    ) || exit 1
fi
